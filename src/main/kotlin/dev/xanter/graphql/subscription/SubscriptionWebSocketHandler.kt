package dev.xanter.graphql.subscription

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Default WebSocket handler for handling GraphQL subscriptions.
 */
class SubscriptionWebSocketHandler(
    private val subscriptionHandler: ApolloSubscriptionProtocolHandler,
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) {
    private val logger: Logger = LoggerFactory.getLogger(SubscriptionWebSocketHandler::class.java)
    val protocol: String = "graphql-ws"

    suspend fun handle(session: WebSocketSession) = coroutineScope {
        for (frame in session.incoming) {
            when (frame) {
                is Frame.Text -> {
                    val request = frame.readText()
                    logger.trace("request $request")
                    launch(Dispatchers.IO) {
                        subscriptionHandler.handle(request, session).collect {
                            val response = objectMapper.writeValueAsString(it)
                            logger.trace("response $response")
                            session.send(Frame.Text(response))
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
