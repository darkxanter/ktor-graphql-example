package dev.xanter.graphql

import com.expediagroup.graphql.generator.annotations.GraphQLDirective
import com.expediagroup.graphql.generator.directives.KotlinFieldDirectiveEnvironment
import com.expediagroup.graphql.generator.directives.KotlinSchemaDirectiveWiring
import dev.xanter.auth.AccessPermission
import dev.xanter.models.UserDto
import graphql.introspection.Introspection
import graphql.schema.DataFetcher
import graphql.schema.GraphQLFieldDefinition

@GraphQLDirective(
    description = "Is Authenticated?",
    locations = [
        Introspection.DirectiveLocation.FIELD_DEFINITION,
    ]
)
annotation class Authenticated(
    vararg val roles: AccessPermission = [],
)

class AuthenticatedSchemaDirectiveWiring : KotlinSchemaDirectiveWiring {
    override fun onField(environment: KotlinFieldDirectiveEnvironment): GraphQLFieldDefinition {
        val field = environment.element
        val requiredRoles = environment.directive.getArgument("roles").argumentValue.value.let { value ->
            val roles = value as Array<*>
            if (roles.isArrayOf<AccessPermission>()) {
                roles.filterIsInstance<AccessPermission>()
            } else {
                null
            }
        } ?: error("Missing roles")

        val originalDataFetcher = environment.getDataFetcher()

        println("onField requiredRoles $requiredRoles")

        val authorisationFetcherFetcher = DataFetcher<Any> { dataEnv ->
            println(dataEnv.field)
            val user =
                dataEnv.graphQlContext.get<ArrayList<UserDto>>("user")?.firstOrNull() ?: throw UnauthorizedException()
            if (requiredRoles.isNotEmpty()) {
                if (!requiredRoles.contains(user.role)) {
                    throw ForbiddenException()
                }
            }
            originalDataFetcher.get(dataEnv)
        }
        environment.setDataFetcher(authorisationFetcherFetcher)
        return field
    }
}

class UnauthorizedException : Exception("Unauthorized")
class ForbiddenException : Exception("Forbidden")
