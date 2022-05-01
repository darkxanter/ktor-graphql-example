package dev.xanter.graphql.subscription

import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import io.ktor.websocket.WebSocketSession

/**
 * Ktor specific code to generate the context for a subscription request
 */
abstract class KtorSubscriptionGraphQLContextFactory<out T : GraphQLContext> :
    GraphQLContextFactory<T, WebSocketSession>

/**
 * Basic implementation of [KtorSubscriptionGraphQLContextFactory] that just returns null
 */
class DefaultKtorSubscriptionGraphQLContextFactory : KtorSubscriptionGraphQLContextFactory<GraphQLContext>() {
    override suspend fun generateContext(request: WebSocketSession): GraphQLContext? = null
}
