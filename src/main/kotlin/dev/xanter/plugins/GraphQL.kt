package dev.xanter.plugins

import com.github.darkxanter.graphql.GraphQLKotlin
import dev.xanter.graphql.AuthorizedContext
import dev.xanter.models.User
import dev.xanter.graphql.schema.ArticleQueryService
import dev.xanter.graphql.schema.HelloQueryService
import dev.xanter.graphql.schema.LoginMutationService
import dev.xanter.graphql.schema.SimpleSubscription
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configureGraphQL() {
    install(GraphQLKotlin) {
        queries = listOf(
            HelloQueryService(),
            ArticleQueryService(),
        )
        mutations = listOf(
            LoginMutationService()
        )

        subscriptions = listOf(
            SimpleSubscription()
        )

        schemaGeneratorConfig {
            supportedPackages = listOf("dev.xanter")
        }

        generateContextMap { request ->
            val loggedInUser = User(
                email = "johndoe@example.com",
                firstName = "John",
                lastName = "Doe",
                universityId = 1,
            )
            mapOf(
                "AuthorizedContext" to AuthorizedContext(loggedInUser)
            )
        }
    }
}
