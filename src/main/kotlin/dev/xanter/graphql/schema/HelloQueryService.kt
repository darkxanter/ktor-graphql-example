@file:Suppress("unused")

package dev.xanter.graphql.schema

import com.expediagroup.graphql.server.operations.Query

class HelloQueryService : Query {
    fun hello() = "World!"
}
