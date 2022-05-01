package dev.xanter.graphql.subscription

import com.expediagroup.graphql.generator.execution.GraphQLContext
import io.ktor.websocket.WebSocketSession

/**
 * Implementation of Apollo Subscription Server Lifecycle Events
 * https://www.apollographql.com/docs/graphql-subscriptions/lifecycle-events/
 */
interface ApolloSubscriptionHooks {

    /**
     * Allows validation of connectionParams prior to starting the connection.
     * You can reject the connection by throwing an exception.
     * If you need to forward state to execution, update and return the [GraphQLContext].
     */
    @Deprecated("The generic context object is deprecated in favor of the context map")
    fun onConnect(
        connectionParams: Map<String, String>,
        session: WebSocketSession,
        graphQLContext: GraphQLContext?
    ): GraphQLContext? = graphQLContext

    /**
     * Allows validation of connectionParams prior to starting the connection.
     * You can reject the connection by throwing an exception.
     * If you need to forward state to execution, update and return the context map.
     */
    fun onConnectWithContext(
        connectionParams: Map<String, String>,
        session: WebSocketSession,
        graphQLContext: Map<*, Any>?
    ): Map<*, Any>? = graphQLContext

    /**
     * Called when the client executes a GraphQL operation.
     * The context can not be updated here, it is read only.
     */
    @Deprecated("The generic context object is deprecated in favor of the context map")
    fun onOperation(
        operationMessage: SubscriptionOperationMessage,
        session: WebSocketSession,
        graphQLContext: GraphQLContext?
    ): Unit = Unit

    /**
     * Called when the client executes a GraphQL operation.
     * The context can not be updated here, it is read only.
     */
    fun onOperationWithContext(
        operationMessage: SubscriptionOperationMessage,
        session: WebSocketSession,
        graphQLContext: Map<*, Any>?
    ): Unit = Unit

    /**
     * Called when client's unsubscribes
     */
    fun onOperationComplete(session: WebSocketSession): Unit = Unit

    /**
     * Called when the client disconnects
     */
    fun onDisconnect(session: WebSocketSession): Unit = Unit
}

/**
 * Default implementation of Apollo Subscription Lifecycle Events.
 */
open class SimpleSubscriptionHooks : ApolloSubscriptionHooks
