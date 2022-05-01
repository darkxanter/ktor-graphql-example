package dev.xanter.schema

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Subscription
import graphql.GraphqlErrorException
import graphql.execution.DataFetcherResult
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactive.asPublisher
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import java.time.Duration
import kotlin.random.Random

class SimpleSubscription : Subscription {

    val logger: Logger = LoggerFactory.getLogger(SimpleSubscription::class.java)

    @GraphQLDescription("Returns a single value")
    fun singleValueSubscription(): Flux<Int> = Flux.just(1)

    @GraphQLDescription("Returns a random number every second")
    fun counter(limit: Int? = null): Flux<Int> {
        val flux = Flux.interval(Duration.ofSeconds(1)).map {
            val value = Random.nextInt()
            logger.info("Returning $value from counter")
            value
        }

        return if (limit != null) {
            flux.take(limit.toLong())
        } else {
            flux
        }
    }

    @GraphQLDescription("Returns a random number every second, errors if even")
    fun counterWithError(): Flux<Int> = Flux.interval(Duration.ofSeconds(1))
        .map {
            val value = Random.nextInt()
            if (value % 2 == 0) {
                throw Exception("Value is even $value")
            } else value
        }

    @GraphQLDescription("Returns one value then an error")
    fun singleValueThenError(): Flux<Int> = Flux.just(1, 2)
        .map { if (it == 2) throw Exception("Second value") else it }

    @GraphQLDescription("Returns stream of values")
    fun flow(): Publisher<Int> = flowOf(1, 2, 4).asPublisher()

    @GraphQLDescription("Returns stream of errors")
    fun flowOfErrors(): Publisher<DataFetcherResult<String?>> {
        val dfr: DataFetcherResult<String?> = DataFetcherResult.newResult<String?>()
            .data(null)
            .error(GraphqlErrorException.newErrorException().cause(Exception("error thrown")).build())
            .build()

        return flowOf(dfr, dfr).asPublisher()
    }

//    @GraphQLDescription("Returns a value from the subscription context")
//    fun subscriptionContext(myGraphQLContext: MySubscriptionGraphQLContext): Flux<String> =
//        Flux.just(myGraphQLContext.auth ?: "no-auth")
}
