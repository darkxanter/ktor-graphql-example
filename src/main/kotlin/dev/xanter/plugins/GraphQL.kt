package dev.xanter.plugins

import com.github.darkxanter.graphql.GraphQLKotlin
import dev.xanter.usecases.ArticleQueryService
import dev.xanter.usecases.CityQueryService
import dev.xanter.usecases.HelloQueryService
import dev.xanter.usecases.SimpleSubscription
import io.ktor.server.application.Application
import io.ktor.server.application.install

fun Application.configureGraphQL() {
    install(GraphQLKotlin) {
        queries = listOf(
            HelloQueryService(),
            ArticleQueryService(),
            CityQueryService(),
        )
        mutations = listOf(
//            LoginMutationService()
        )

        subscriptions = listOf(
            SimpleSubscription()
        )

        schemaGeneratorConfig {
            supportedPackages = listOf("dev.xanter")
        }

        generateContextMap { request ->
//            val loggedInUser = User(
//                email = "johndoe@example.com",
//                firstName = "John",
//                lastName = "Doe",
//                universityId = 1,
//            )
//            mapOf(
//                "AuthorizedContext" to AuthorizedContext(loggedInUser)
//            )
            emptyMap()
        }
    }
}
