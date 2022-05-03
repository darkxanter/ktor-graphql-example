package dev.xanter.graphql.subscription

import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.flow.Flow

interface SubscriptionProtocolHandler<TMessage> {
    suspend fun handle(payload: String, session: WebSocketSession): Flow<TMessage>
}
