@file:Suppress("unused")

package dev.xanter.usecases

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import dev.xanter.models.Cities
import dev.xanter.models.City
import dev.xanter.models.CityDto
import dev.xanter.models.UserDto
import dev.xanter.models.Users
import graphql.schema.DataFetchingEnvironment
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class CityQueryService : Query {
    @GraphQLDescription("List of cities")
    suspend fun citiesDao(dataFetchingEnvironment: DataFetchingEnvironment): List<CityDto> {
        val selectedFields = dataFetchingEnvironment.selectedFields()
        println("selectedFields $selectedFields")

        return newSuspendedTransaction {
            val cities = City.all()

            cities.map { city ->
                CityDto(
                    name = city.name,
                    users = if (selectedFields.containsKey("users")) {
                        city.users.map { user ->
                            UserDto(
                                name = user.name,
                                age = user.age,
                            )
                        }
                    } else emptyList(),
                )
            }
        }
    }

    suspend fun citiesDaoPreload(dataFetchingEnvironment: DataFetchingEnvironment): List<CityDto> {
        val selectedFields = dataFetchingEnvironment.selectedFields()
        println("selectedFields $selectedFields")

        return newSuspendedTransaction {
            val cities = City.all().let {
                if (selectedFields.containsKey(CityDto::users.name)) {
                    it.with(City::users)
                } else {
                    it
                }
            }

            cities.map { city ->
                CityDto(
                    name = city.name,
                    users = if (selectedFields.containsKey("users")) {
                        city.users.map { user ->
                            UserDto(
                                name = user.name,
                                age = user.age,
                            )
                        }
                    } else emptyList(),
                )
            }
        }
    }

    @GraphQLDescription("List of cities")
    suspend fun citiesDsl(dataFetchingEnvironment: DataFetchingEnvironment): List<CityDto> {
        val selectedFields = dataFetchingEnvironment.selectedFields()
        println("selectedFields $selectedFields")

        return newSuspendedTransaction {
            val table = if (selectedFields.containsKey(CityDto::users.name)) {
                Cities.leftJoin(Users)
            } else {
                Cities
            }

            table.selectAll().map { row ->
                CityDto(
                    name = row[Cities.name],
                    users = if (selectedFields.containsKey("users")) {
                        listOf(
                            UserDto(
                                name = row[Users.name],
                                age = row[Users.age],
                            )
                        )
                    } else emptyList(),
                )
            }
        }
    }
}
