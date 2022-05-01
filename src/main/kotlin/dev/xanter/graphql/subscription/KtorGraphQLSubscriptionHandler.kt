package dev.xanter.graphql.subscription

import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import com.expediagroup.graphql.server.extensions.toExecutionInput
import com.expediagroup.graphql.server.extensions.toGraphQLError
import com.expediagroup.graphql.server.extensions.toGraphQLKotlinType
import com.expediagroup.graphql.server.extensions.toGraphQLResponse
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLResponse
import graphql.ExecutionResult
import graphql.GraphQL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import org.reactivestreams.Publisher

/**
 * Default Ktor implementation of GraphQL subscription handler.
 */
open class KtorGraphQLSubscriptionHandler(
    private val graphQL: GraphQL,
    private val dataLoaderRegistryFactory: DataLoaderRegistryFactory? = null
) {

    fun executeSubscription(graphQLRequest: GraphQLRequest, graphQLContext: GraphQLContext?, graphQLContextMap: Map<*, Any>? = null): Flow<GraphQLResponse<*>> {
        val dataLoaderRegistry = dataLoaderRegistryFactory?.generate()
        val input = graphQLRequest.toExecutionInput(graphQLContext, dataLoaderRegistry, graphQLContextMap)

        return graphQL.execute(input)
            .getData<Publisher<ExecutionResult>>()
            .asFlow()
            .map { result -> result.toGraphQLResponse() }
            .catch { throwable ->
                val error = throwable.toGraphQLError()
                emit(GraphQLResponse<Any?>(errors = listOf(error.toGraphQLKotlinType())))
            }
    }
}