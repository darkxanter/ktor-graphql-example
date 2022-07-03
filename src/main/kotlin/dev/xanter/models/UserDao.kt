package dev.xanter.models

import dev.xanter.auth.AccessPermission
import dev.xanter.graphql.Authenticated
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object Users : IntIdTable() {
    val name = varchar("name", 50).index()
    val email = varchar("email", 50)
    val city = reference("city", Cities)
    val age = integer("age")
    val role = enumerationByName("role", 10, AccessPermission::class)
}

class UserDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserDao>(Users)

    var name by Users.name
    var email by Users.email
    var city by CityDao referencedOn Users.city
    var age by Users.age
    var role by Users.role
}

data class UserDto(
    val name: String,
    val age: Int,
    @Authenticated
    val email: String,
    @Authenticated(AccessPermission.Admin)
    val role: AccessPermission,
) {
    companion object {
        suspend fun search(ids: List<Int>): List<UserDto> {
            return newSuspendedTransaction {
                UserDao.find {
                    Users.id inList ids
                }.toDto()
            }
        }
    }
}

fun UserDao.toDto() = UserDto(name = name, age = age, email = email, role = role)
fun Iterable<UserDao>.toDto() = map { it.toDto() }

