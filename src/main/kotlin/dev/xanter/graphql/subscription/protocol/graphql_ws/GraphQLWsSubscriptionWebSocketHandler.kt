package dev.xanter.graphql.subscription.protocol.graphql_ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.xanter.graphql.subscription.SubscriptionWebSocketHandler

class GraphQLWsSubscriptionWebSocketHandler(
    subscriptionHandler: GraphQLWsSubscriptionProtocolHandler,
    objectMapper: ObjectMapper = jacksonObjectMapper(),
) : SubscriptionWebSocketHandler<SubscriptionOperationMessage>(
    subscriptionHandler, objectMapper
) {
    override val protocol: String = "graphql-ws"
}
