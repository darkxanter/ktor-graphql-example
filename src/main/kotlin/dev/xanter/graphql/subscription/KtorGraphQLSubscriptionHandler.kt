package dev.xanter.graphql.subscription

import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import com.expediagroup.graphql.server.extensions.toExecutionInput
import com.expediagroup.graphql.server.extensions.toGraphQLError
import com.expediagroup.graphql.server.extensions.toGraphQLKotlinType
import com.expediagroup.graphql.server.extensions.toGraphQLResponse
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLResponse
import dev.xanter.graphql.subscription.protocol.graphql_transport_ws.GraphQLRequestWS
import dev.xanter.graphql.subscription.protocol.graphql_transport_ws.toExecutionInput
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlin.reflect.typeOf

/**
 * Default Ktor implementation of GraphQL subscription handler.
 */
open class KtorGraphQLSubscriptionHandler(
    private val graphQL: GraphQL,
    private val dataLoaderRegistryFactory: DataLoaderRegistryFactory? = null
) {

    fun executeSubscription(
        graphQLRequest: GraphQLRequest,
        graphQLContext: GraphQLContext?,
        graphQLContextMap: Map<*, Any>? = null
    ): Flow<GraphQLResponse<*>> {
        val dataLoaderRegistry = dataLoaderRegistryFactory?.generate()
        val input = graphQLRequest.toExecutionInput(graphQLContext, dataLoaderRegistry, graphQLContextMap)
        return execute(input)
    }

    fun executeSubscription(
        graphQLRequest: GraphQLRequestWS,
        graphQLContext: GraphQLContext?,
        graphQLContextMap: Map<*, Any>? = null
    ): Flow<GraphQLResponse<*>> {
        val dataLoaderRegistry = dataLoaderRegistryFactory?.generate()
        val input = graphQLRequest.toExecutionInput(graphQLContext, dataLoaderRegistry, graphQLContextMap)
        return execute(input)
    }

    private fun execute(input: ExecutionInput): Flow<GraphQLResponse<*>> {
        val executionResult = graphQL.execute(input)
        val data = executionResult.getData<Any>()
        @Suppress("UNCHECKED_CAST")
        return when (data) {
            is Flow<*> -> (data as Flow<ExecutionResult>).map { result -> result.toGraphQLResponse() }
                .catch { throwable ->
                    val error = throwable.toGraphQLError()
                    emit(GraphQLResponse<Any?>(errors = listOf(error.toGraphQLKotlinType())))
                }
            else -> {
                val response = try {
                    executionResult.toGraphQLResponse()
                } catch (e: Exception) {
                    val error = e.toGraphQLError()
                    GraphQLResponse<Any?>(errors = listOf(error.toGraphQLKotlinType()))
                }
                flowOf(response)
            }
        }
    }
}