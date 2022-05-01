package dev.xanter.graphql.subscription

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import org.reactivestreams.Subscription
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

internal class ApolloSubscriptionSessionState {

    // Sessions are saved by web socket session id
    @Suppress("MemberVisibilityCanBePrivate")
    internal val activeKeepAliveSessions = ConcurrentHashMap<WebSocketSession, Subscription>()

    // Operations are saved by web socket session id, then operation id
    @Suppress("MemberVisibilityCanBePrivate")
    internal val activeOperations = ConcurrentHashMap<WebSocketSession, ConcurrentHashMap<String, Subscription>>()

    // The context is saved by web socket session id
    private val cachedContext = ConcurrentHashMap<WebSocketSession, GraphQLContext>()

    // The graphQL context is saved by web socket session id
    private val cachedGraphQLContext = ConcurrentHashMap<WebSocketSession, Map<*, Any>>()

    /**
     * Save the context created from the factory and possibly updated in the onConnect hook.
     * This allows us to include some initial state to be used when handling all the messages.
     * This will be removed in [terminateSession].
     */
    fun saveContext(session: WebSocketSession, graphQLContext: GraphQLContext?) {
        if (graphQLContext != null) {
            cachedContext[session] = graphQLContext
        }
    }

    /**
     * Save the context created from the factory and possibly updated in the onConnect hook.
     * This allows us to include some initial state to be used when handling all the messages.
     * This will be removed in [terminateSession].
     */
    fun saveContextMap(session: WebSocketSession, graphQLContext: Map<*, Any>?) {
        if (graphQLContext != null) {
            cachedGraphQLContext[session] = graphQLContext
        }
    }

    /**
     * Return the context for this session.
     */
    fun getContext(session: WebSocketSession): GraphQLContext? = cachedContext[session]

    /**
     * Return the graphQL context for this session.
     */
    fun getGraphQLContext(session: WebSocketSession): Map<*, Any>? = cachedGraphQLContext[session]

    /**
     * Save the session that is sending keep alive messages.
     * This will override values without cancelling the subscription, so it is the responsibility of the consumer to cancel.
     * These messages will be stopped on [terminateSession].
     */
    fun saveKeepAliveSubscription(session: WebSocketSession, subscription: Subscription) {
        activeKeepAliveSessions[session] = subscription
    }

    /**
     * Save the operation that is sending data to the client.
     * This will override values without cancelling the subscription, so it is the responsibility of the consumer to cancel.
     * These messages will be stopped on [stopOperation].
     */
    fun saveOperation(session: WebSocketSession, operationMessage: SubscriptionOperationMessage, subscription: Subscription) {
        val id = operationMessage.id
        if (id != null) {
            val operationsForSession: ConcurrentHashMap<String, Subscription> = activeOperations.getOrPut(session) { ConcurrentHashMap() }
            operationsForSession[id] = subscription
        }
    }

    /**
     * Send the [SubscriptionOperationMessage.ServerMessages.GQL_COMPLETE] message.
     * This can happen when the publisher finishes or if the client manually sends the stop message.
     */
    fun completeOperation(session: WebSocketSession, operationMessage: SubscriptionOperationMessage): Mono<SubscriptionOperationMessage> {
        return getCompleteMessage(operationMessage)
            .doFinally { removeActiveOperation(session, operationMessage.id, cancelSubscription = false) }
    }

    /**
     * Stop the subscription sending data and send the [SubscriptionOperationMessage.ServerMessages.GQL_COMPLETE] message.
     * Does NOT terminate the session.
     */
    fun stopOperation(session: WebSocketSession, operationMessage: SubscriptionOperationMessage): Mono<SubscriptionOperationMessage> {
        return getCompleteMessage(operationMessage)
            .doFinally { removeActiveOperation(session, operationMessage.id, cancelSubscription = true) }
    }

    private fun getCompleteMessage(operationMessage: SubscriptionOperationMessage): Mono<SubscriptionOperationMessage> {
        val id = operationMessage.id
        if (id != null) {
            return Mono.just(SubscriptionOperationMessage(type = SubscriptionOperationMessage.ServerMessages.GQL_COMPLETE.type, id = id))
        }
        return Mono.empty()
    }

    /**
     * Remove active running subscription from the cache and cancel if needed
     */
    private fun removeActiveOperation(session: WebSocketSession, id: String?, cancelSubscription: Boolean) {
        val operationsForSession = activeOperations[session]
        val subscription = operationsForSession?.get(id)
        if (subscription != null) {
            if (cancelSubscription) {
                subscription.cancel()
            }
            operationsForSession.remove(id)
            if (operationsForSession.isEmpty()) {
                activeOperations.remove(session)
            }
        }
    }

    /**
     * Terminate the session, cancelling the keep alive messages and all operations active for this session.
     */
    suspend fun terminateSession(session: WebSocketSession) {
        activeOperations[session]?.forEach { (_, subscription) -> subscription.cancel() }
        activeOperations.remove(session)
        cachedContext.remove(session)
        cachedGraphQLContext.remove(session)
        activeKeepAliveSessions[session]?.cancel()
        activeKeepAliveSessions.remove(session)
        session.close()
    }

    /**
     * Looks up the operation for the client, to check if it already exists
     */
    fun doesOperationExist(session: WebSocketSession, operationMessage: SubscriptionOperationMessage): Boolean =
        activeOperations[session]?.containsKey(operationMessage.id) ?: false
}
