package dev.xanter

import dev.xanter.models.Cities
import dev.xanter.models.City
import dev.xanter.models.User
import dev.xanter.models.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun initDatabase() {
//    Database.connect("jdbc:h2:file:./demodb", driver = "org.h2.Driver", user = "root", password = "")
    Database.connect("jdbc:sqlite:./data.db", "org.sqlite.JDBC")


    transaction {
        SchemaUtils.create (Cities, Users)

        if (City.all().empty()) {
            val stPete = City.new {
                name = "St. Petersburg"
            }

            val munich = City.new {
                name = "Munich"
            }

            User.new {
                name = "a"
                city = stPete
                age = 5
            }

            User.new {
                name = "b"
                city = stPete
                age = 27
            }

            User.new {
                name = "c"
                city = munich
                age = 42
            }
        }

        println("Cities: ${City.all().joinToString {it.name}}")
//        println("Users in ${stPete.name}: ${stPete.users.joinToString {it.name}}")
        println("Adults: ${User.find { Users.age greaterEq 18 }.joinToString {it.name}}")
    }
}
