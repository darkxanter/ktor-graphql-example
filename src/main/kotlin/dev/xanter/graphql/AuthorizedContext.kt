package dev.xanter.graphql

import dev.xanter.models.UserDto

data class UserContext(
    val user: UserDto? = null,
)
