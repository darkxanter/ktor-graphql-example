package dev.xanter.graphql

import dev.xanter.models.User

data class AuthorizedContext(
    val authorizedUser: User? = null,
    var guestUUID: String? = null,
    val customHeader: String? = null
)
