package dev.xanter.plugins

import com.expediagroup.graphql.generator.directives.KotlinDirectiveWiringFactory
import com.expediagroup.graphql.generator.directives.KotlinSchemaDirectiveWiring
import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.github.darkxanter.graphql.GraphQLKotlin
import dev.xanter.auth.AccessPermission
import dev.xanter.graphql.AuthenticatedSchemaDirectiveWiring
import dev.xanter.graphql.dataloaders.CityDataLoader
import dev.xanter.graphql.dataloaders.UserDataLoader
import dev.xanter.models.UserDao
import dev.xanter.models.Users
import dev.xanter.models.toDto
import dev.xanter.usecases.ArticleQueryService
import dev.xanter.usecases.CityQueryService
import dev.xanter.usecases.HelloQueryService
import dev.xanter.usecases.SimpleSubscription
import dev.xanter.usecases.SomeMutation
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureGraphQL() {
    install(GraphQLKotlin) {
        queries = listOf(
            HelloQueryService(),
            ArticleQueryService(),
            CityQueryService(),
        )
        mutations = listOf(
            SomeMutation()
//            LoginMutationService()
        )

        subscriptions = listOf(
            SimpleSubscription()
        )

        dataLoaders = listOf(
            UserDataLoader,
            CityDataLoader,
        )

        schemaGeneratorConfig {
            val customWiringFactory = KotlinDirectiveWiringFactory(
                manualWiring = mapOf<String, KotlinSchemaDirectiveWiring>(
                    "authenticated" to AuthenticatedSchemaDirectiveWiring()
                )
            )

            supportedPackages = listOf("dev.xanter")
            hooks = object : FlowSubscriptionSchemaGeneratorHooks() {
                override val wiringFactory: KotlinDirectiveWiringFactory
                    get() = customWiringFactory
            }
        }

        generateContextMap { request ->
//            val loggedInUser = User(
//                email = "johndoe@example.com",
//                firstName = "John",
//                lastName = "Doe",
//                universityId = 1,
//            )

            val user = transaction {
                UserDao.find { Users.role eq AccessPermission.Admin }.toDto()
            }
            mapOf<Any, Any>(
                "user" to user
            ).also {
                println("context $it")
            }
        }
    }
}
