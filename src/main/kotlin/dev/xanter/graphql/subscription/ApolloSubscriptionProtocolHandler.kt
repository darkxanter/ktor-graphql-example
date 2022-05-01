package dev.xanter.graphql.subscription

import com.expediagroup.graphql.server.types.GraphQLRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import dev.xanter.graphql.GraphQLConfigurationProperties
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.reactor.asFlux
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import java.time.Duration
import dev.xanter.graphql.subscription.SubscriptionOperationMessage.ClientMessages
import dev.xanter.graphql.subscription.SubscriptionOperationMessage.ServerMessages
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

/**
 * Implementation of the `graphql-ws` protocol defined by Apollo
 * https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md
 */
class ApolloSubscriptionProtocolHandler(
    private val config: GraphQLConfigurationProperties,
    private val contextFactory: KtorSubscriptionGraphQLContextFactory<*>,
    private val subscriptionHandler: KtorGraphQLSubscriptionHandler,
    private val objectMapper: ObjectMapper,
    private val subscriptionHooks: ApolloSubscriptionHooks
) {
    private val sessionState = ApolloSubscriptionSessionState()
    private val logger = LoggerFactory.getLogger(ApolloSubscriptionProtocolHandler::class.java)
    private val keepAliveMessage = SubscriptionOperationMessage(type = ServerMessages.GQL_CONNECTION_KEEP_ALIVE.type)
    private val basicConnectionErrorMessage = SubscriptionOperationMessage(type = ServerMessages.GQL_CONNECTION_ERROR.type)
    private val acknowledgeMessage = SubscriptionOperationMessage(ServerMessages.GQL_CONNECTION_ACK.type)

    @ExperimentalCoroutinesApi
    @Suppress("Detekt.TooGenericExceptionCaught")
    suspend fun handle(payload: String, session: WebSocketSession): Flux<SubscriptionOperationMessage> {
        val operationMessage = convertToMessageOrNull(payload) ?: return Flux.just(basicConnectionErrorMessage)
        logger.debug("GraphQL subscription client, session=$session operationMessage=$operationMessage")

        return try {
            when (operationMessage.type) {
                ClientMessages.GQL_CONNECTION_INIT.type -> onInit(operationMessage, session)
                ClientMessages.GQL_START.type -> startSubscription(operationMessage, session)
                ClientMessages.GQL_STOP.type -> onStop(operationMessage, session)
                ClientMessages.GQL_CONNECTION_TERMINATE.type -> onDisconnect(session)
                else -> onUnknownOperation(operationMessage, session)
            }
        } catch (exception: Exception) {
            onException(exception)
        }
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private fun convertToMessageOrNull(payload: String): SubscriptionOperationMessage? {
        return try {
            objectMapper.readValue(payload)
        } catch (exception: Exception) {
            logger.error("Error parsing the subscription message", exception)
            null
        }
    }

    /**
     * If the keep alive configuration is set, send a message back to client at every interval until the session is terminated.
     * Otherwise just return empty flux to append to the acknowledge message.
     */
    private fun getKeepAliveFlux(session: WebSocketSession): Flux<SubscriptionOperationMessage> {
        val keepAliveInterval: Long? = config.subscriptions.keepAliveInterval
        if (keepAliveInterval != null) {
            return Flux.interval(Duration.ofMillis(keepAliveInterval))
                .map { keepAliveMessage }
                .doOnSubscribe { sessionState.saveKeepAliveSubscription(session, it) }
        }

        return Flux.empty()
    }

    @Suppress("Detekt.TooGenericExceptionCaught")
    private fun startSubscription(
        operationMessage: SubscriptionOperationMessage,
        session: WebSocketSession
    ): Flux<SubscriptionOperationMessage> {
        val context = sessionState.getContext(session)
        val graphQLContext = sessionState.getGraphQLContext(session)

        subscriptionHooks.onOperation(operationMessage, session, context)
        subscriptionHooks.onOperationWithContext(operationMessage, session, graphQLContext)

        if (operationMessage.id == null) {
            logger.error("GraphQL subscription operation id is required")
            return Flux.just(basicConnectionErrorMessage)
        }

        if (sessionState.doesOperationExist(session, operationMessage)) {
            logger.info("Already subscribed to operation ${operationMessage.id} for session ${session}")
            return Flux.empty()
        }

        val payload = operationMessage.payload

        if (payload == null) {
            logger.error("GraphQL subscription payload was null instead of a GraphQLRequest object")
            sessionState.stopOperation(session, operationMessage)
            return Flux.just(SubscriptionOperationMessage(type = ServerMessages.GQL_CONNECTION_ERROR.type, id = operationMessage.id))
        }

        try {
            val request = objectMapper.convertValue<GraphQLRequest>(payload)
            return subscriptionHandler.executeSubscription(request, context, graphQLContext)
                .asFlux()
                .map {
                    if (it.errors?.isNotEmpty() == true) {
                        SubscriptionOperationMessage(type = ServerMessages.GQL_ERROR.type, id = operationMessage.id, payload = it)
                    } else {
                        SubscriptionOperationMessage(type = ServerMessages.GQL_DATA.type, id = operationMessage.id, payload = it)
                    }
                }
                .concatWith(onComplete(operationMessage, session).toFlux())
                .doOnSubscribe { sessionState.saveOperation(session, operationMessage, it) }
        } catch (exception: Exception) {
            logger.error("Error running graphql subscription", exception)
            // Do not terminate the session, just stop the operation messages
            sessionState.stopOperation(session, operationMessage)
            return Flux.just(SubscriptionOperationMessage(type = ServerMessages.GQL_CONNECTION_ERROR.type, id = operationMessage.id))
        }
    }

    private fun onInit(operationMessage: SubscriptionOperationMessage, session: WebSocketSession): Flux<SubscriptionOperationMessage> {
        saveContext(operationMessage, session)
        val acknowledgeMessage = Mono.just(acknowledgeMessage)
        val keepAliveFlux = getKeepAliveFlux(session)
        return acknowledgeMessage.concatWith(keepAliveFlux)
            .onErrorReturn(getConnectionErrorMessage(operationMessage))
    }

    /**
     * Generate the context and save it for all future messages.
     */
    private fun saveContext(operationMessage: SubscriptionOperationMessage, session: WebSocketSession) {
        runBlocking {
            val connectionParams = castToMapOfStringString(operationMessage.payload)
            val context = contextFactory.generateContext(session)
            val graphQLContext = contextFactory.generateContextMap(session)
            val onConnectContext = subscriptionHooks.onConnect(connectionParams, session, context)
            val onConnectGraphQLContext = subscriptionHooks.onConnectWithContext(connectionParams, session, graphQLContext)
            sessionState.saveContext(session, onConnectContext)
            sessionState.saveContextMap(session, onConnectGraphQLContext)
        }
    }

    /**
     * Called with the publisher has completed on its own.
     */
    private fun onComplete(
        operationMessage: SubscriptionOperationMessage,
        session: WebSocketSession
    ): Mono<SubscriptionOperationMessage> {
        subscriptionHooks.onOperationComplete(session)
        return sessionState.completeOperation(session, operationMessage)
    }

    /**
     * Called with the client has called stop manually, or on error, and we need to cancel the publisher
     */
    private fun onStop(
        operationMessage: SubscriptionOperationMessage,
        session: WebSocketSession
    ): Flux<SubscriptionOperationMessage> {
        subscriptionHooks.onOperationComplete(session)
        return sessionState.stopOperation(session, operationMessage).toFlux()
    }

    private suspend fun onDisconnect(session: WebSocketSession): Flux<SubscriptionOperationMessage> {
        subscriptionHooks.onDisconnect(session)
        sessionState.terminateSession(session)
        return Flux.empty()
    }

    private fun onUnknownOperation(operationMessage: SubscriptionOperationMessage, session: WebSocketSession): Flux<SubscriptionOperationMessage> {
        logger.error("Unknown subscription operation $operationMessage")
        sessionState.stopOperation(session, operationMessage)
        return Flux.just(getConnectionErrorMessage(operationMessage))
    }

    private fun onException(exception: Exception): Flux<SubscriptionOperationMessage> {
        logger.error("Error parsing the subscription message", exception)
        return Flux.just(basicConnectionErrorMessage)
    }

    private fun getConnectionErrorMessage(operationMessage: SubscriptionOperationMessage): SubscriptionOperationMessage {
        return SubscriptionOperationMessage(type = ServerMessages.GQL_CONNECTION_ERROR.type, id = operationMessage.id)
    }
}
