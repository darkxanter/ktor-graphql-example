package dev.xanter.graphql

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.execution.FlowSubscriptionExecutionStrategy
import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import dev.xanter.schema.HelloQueryService
import dev.xanter.schema.LoginMutationService
import dev.xanter.schema.SimpleSubscription
import graphql.GraphQL

/**
 * Custom logic for how this Ktor server loads all the queries and configuration to create the [GraphQL] object
 * needed to handle incoming requests. In a more enterprise solution you may want to load more things from
 * configuration files instead of hardcoding them.
 */
private val config = SchemaGeneratorConfig(
    supportedPackages = listOf("dev.xanter"),
    hooks = FlowSubscriptionSchemaGeneratorHooks(),
)
private val queries = listOf(
    HelloQueryService(),
//    TopLevelObject(BookQueryService()),
//    TopLevelObject(CourseQueryService()),
//    TopLevelObject(UniversityQueryService())
)
private val mutations = listOf(
    LoginMutationService()
)
private val subscriptions = listOf(
    SimpleSubscription()
)

private val graphQLSchema = toSchema(
    config,
    queries.toTopLevelObjects(),
    mutations.toTopLevelObjects(),
    subscriptions.toTopLevelObjects(),
)

fun getGraphQLObject(): GraphQL = GraphQL
    .newGraphQL(graphQLSchema)
    .subscriptionExecutionStrategy(FlowSubscriptionExecutionStrategy())
    .build()

private fun List<Any>.toTopLevelObjects() = map { TopLevelObject(it) }
