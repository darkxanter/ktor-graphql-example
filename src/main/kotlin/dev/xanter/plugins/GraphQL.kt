package dev.xanter.plugins

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.xanter.graphql.GraphQLConfigurationProperties
import dev.xanter.graphql.KtorServer
import dev.xanter.graphql.getGraphQLObject
import dev.xanter.graphql.subscription.ApolloSubscriptionProtocolHandler
import dev.xanter.graphql.subscription.DefaultKtorSubscriptionGraphQLContextFactory
import dev.xanter.graphql.subscription.KtorGraphQLSubscriptionHandler
import dev.xanter.graphql.subscription.SimpleSubscriptionHooks
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.log
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow

@OptIn(ExperimentalCoroutinesApi::class)
fun Application.configureGraphQL() {
    val subscriptionProtocolHandler = ApolloSubscriptionProtocolHandler(
        GraphQLConfigurationProperties(
            packages = listOf("dev.xanter")
        ),
        DefaultKtorSubscriptionGraphQLContextFactory(),
        KtorGraphQLSubscriptionHandler(getGraphQLObject()),
        jacksonObjectMapper(),
        SimpleSubscriptionHooks(),
    )

    val mapper = jacksonObjectMapper()


    routing {
        post("graphql") {
            KtorServer().handle(this.call)
        }

        get("playground") {
            this.call.respondText(buildPlaygroundHtml("graphql", "subscriptions"), ContentType.Text.Html)
        }

        webSocket("/subscriptions", protocol = "graphql-ws") {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> {
                        val text = frame.readText()
                        application.log.debug("subscriptions request $text")
                        launch(Dispatchers.IO) {
                            subscriptionProtocolHandler.handle(text, this@webSocket).asFlow().collect {
                                val json = mapper.writeValueAsString(it)
                                application.log.debug("subscriptions response $json")
                                send(Frame.Text(json))
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}


@Suppress("SameParameterValue")
private fun buildPlaygroundHtml(graphQLEndpoint: String, subscriptionsEndpoint: String) =
    Application::class.java.classLoader.getResource("graphql-playground.html")?.readText()
        ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
        ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
        ?: throw IllegalStateException("graphql-playground.html cannot be found in the classpath")
