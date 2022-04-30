package dev.xanter.graphql

import com.expediagroup.graphql.generator.execution.GraphQLContext
import dev.xanter.models.User

/**
 * Example of a custom [GraphQLContext]
 */
data class AuthorizedContext(
    val authorizedUser: User? = null,
    var guestUUID: String? = null,
    val customHeader: String? = null
) : GraphQLContext
