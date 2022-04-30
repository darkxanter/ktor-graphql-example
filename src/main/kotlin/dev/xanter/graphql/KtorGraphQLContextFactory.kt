package dev.xanter.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext
import com.expediagroup.graphql.server.execution.GraphQLContextFactory
import dev.xanter.models.User
import io.ktor.server.request.ApplicationRequest

/**
 * Custom logic for how this example app should create its context given the [ApplicationRequest]
 */
class KtorGraphQLContextFactory : GraphQLContextFactory<AuthorizedContext, ApplicationRequest> {

    override suspend fun generateContext(request: ApplicationRequest): AuthorizedContext {
        val loggedInUser = User(
            email = "fake@site.com",
            firstName = "Someone",
            lastName = "You Don't know",
            universityId = 4
        )

        // Parse any headers from the Ktor request
        val customHeader: String? = request.headers["my-custom-header"]

        return AuthorizedContext(loggedInUser, customHeader = customHeader)
    }
}
