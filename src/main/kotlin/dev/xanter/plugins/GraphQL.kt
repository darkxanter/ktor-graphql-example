package dev.xanter.plugins

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.xanter.graphql.GraphQLConfigurationProperties
import dev.xanter.graphql.KtorServer
import dev.xanter.graphql.getGraphQLObject
import dev.xanter.graphql.subscription.protocol.graphql_ws.GraphQLWsSubscriptionProtocolHandler
import dev.xanter.graphql.subscription.DefaultKtorSubscriptionGraphQLContextFactory
import dev.xanter.graphql.subscription.KtorGraphQLSubscriptionHandler
import dev.xanter.graphql.subscription.SimpleSubscriptionHooks
import dev.xanter.graphql.subscription.SubscriptionWebSocketHandler
import dev.xanter.graphql.subscription.protocol.graphql_transport_ws.GraphQLTransportWsSubscriptionProtocolHandler
import dev.xanter.graphql.subscription.protocol.graphql_transport_ws.GraphQLTransportWsSubscriptionWebSocketHandler
import dev.xanter.graphql.subscription.protocol.graphql_ws.GraphQLWsSubscriptionWebSocketHandler
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket

fun Application.configureGraphQL() {
    val graphQLWsSubscriptionProtocolHandler = GraphQLWsSubscriptionProtocolHandler(
        GraphQLConfigurationProperties(
            packages = listOf("dev.xanter")
        ),
        DefaultKtorSubscriptionGraphQLContextFactory(),
        KtorGraphQLSubscriptionHandler(getGraphQLObject()),
        jacksonObjectMapper(),
        SimpleSubscriptionHooks(),
    )

    val graphQLWsSubscriptionHandler = GraphQLWsSubscriptionWebSocketHandler(
        graphQLWsSubscriptionProtocolHandler,
    )

    val graphQLTransportWsSubscriptionProtocolHandler = GraphQLTransportWsSubscriptionProtocolHandler(
        DefaultKtorSubscriptionGraphQLContextFactory(),
        KtorGraphQLSubscriptionHandler(getGraphQLObject()),
        jacksonObjectMapper(),
    )

    val graphQLTransportWsSubscriptionHandler = GraphQLTransportWsSubscriptionWebSocketHandler(
        graphQLTransportWsSubscriptionProtocolHandler,
    )

    routing {
        post("graphql") {
            KtorServer().handle(this.call)
        }

        get("/") {
            this.call.respondText(buildPlaygroundHtml("graphql", "subscriptions"), ContentType.Text.Html)
        }

        webSocket("/subscriptions", protocol = graphQLWsSubscriptionHandler.protocol) {
            graphQLWsSubscriptionHandler.handle(this)
        }

        webSocket("/subscriptions", protocol = graphQLTransportWsSubscriptionHandler.protocol) {
            graphQLTransportWsSubscriptionHandler.handle(this)
        }
    }
}


@Suppress("SameParameterValue")
private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")
