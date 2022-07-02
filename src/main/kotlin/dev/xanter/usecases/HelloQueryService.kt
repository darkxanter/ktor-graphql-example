@file:Suppress("unused")

package dev.xanter.usecases

import com.expediagroup.graphql.server.operations.Query

class HelloQueryService : Query {
    fun hello() = "World!"
}
