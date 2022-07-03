package dev.xanter.usecases

import com.expediagroup.graphql.server.operations.Mutation
import dev.xanter.auth.AccessPermission
import dev.xanter.graphql.Authenticated

data class MessageDto(val message: String)

class SomeMutation : Mutation {

    fun sendMessage(
        message: String
    ): MessageDto {
        println("sendMessage $message")
        return MessageDto(message)
    }

    @Authenticated(AccessPermission.Admin)
    fun sendAdminMessage(
        message: String
    ): MessageDto {
        println("sendAdminMessage $message")
        return MessageDto(message)
    }
}
