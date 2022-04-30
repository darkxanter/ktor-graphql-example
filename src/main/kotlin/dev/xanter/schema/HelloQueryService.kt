package dev.xanter.schema

import com.expediagroup.graphql.server.operations.Query

class HelloQueryService : Query {
    fun hello() = "World!"
}
