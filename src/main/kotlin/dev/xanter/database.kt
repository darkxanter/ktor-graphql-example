package dev.xanter

import dev.xanter.auth.AccessPermission
import dev.xanter.models.Cities
import dev.xanter.models.CityDao
import dev.xanter.models.UserDao
import dev.xanter.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase() {
//    Database.connect("jdbc:h2:file:./demodb", driver = "org.h2.Driver", user = "root", password = "")
    Database.connect("jdbc:sqlite:./data.db", "org.sqlite.JDBC")


    transaction {
        SchemaUtils.create (Cities, Users)

        if (CityDao.all().empty()) {
            val stPete = CityDao.new {
                name = "St. Petersburg"
            }

            val munich = CityDao.new {
                name = "Munich"
            }

            UserDao.new {
                name = "John Doe"
                city = stPete
                age = 5
                email = "john@example.com"
                role = AccessPermission.User
            }

            UserDao.new {
                name = "Robert"
                city = stPete
                age = 27
                email = "rob@example.com"
                role = AccessPermission.User
            }

            UserDao.new {
                name = "Jack"
                city = munich
                age = 42
                email = "jack@example.com"
                role = AccessPermission.Admin
            }
        }

        println("Cities: ${CityDao.all().joinToString {it.name}}")
//        println("Users in ${stPete.name}: ${stPete.users.joinToString {it.name}}")
        println("Adults: ${UserDao.find { Users.age greaterEq 18 }.joinToString {it.name}}")
    }
}
