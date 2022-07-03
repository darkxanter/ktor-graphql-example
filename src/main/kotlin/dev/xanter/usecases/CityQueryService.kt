@file:Suppress("unused")

package dev.xanter.usecases

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import dev.xanter.graphql.Authenticated
import dev.xanter.models.Cities
import dev.xanter.models.CityDao
import dev.xanter.models.CityDto
import dev.xanter.models.CityLazyDto
import dev.xanter.models.CityLoaderDto
import dev.xanter.models.UserDao
import dev.xanter.models.UserDto
import dev.xanter.models.Users
import dev.xanter.models.toDto
import graphql.schema.DataFetchingEnvironment
import org.jetbrains.exposed.dao.with
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class Nested {
    suspend fun cities(dataFetchingEnvironment: DataFetchingEnvironment): List<CityDto> {
        val selectedFields = dataFetchingEnvironment.selectedFields()
        return newSuspendedTransaction {
            val cities = selectedFields.whenField(
                CityDto::users,
                { CityDao.all().with(CityDao::users) },
                { CityDao.all() }
            )

            cities.map { city ->
                CityDto(
                    name = city.name,
                    users = selectedFields.whenField(
                        CityDto::users,
                        { city.users.toDto() },
                        { emptyList() },
                    )
                )
            }
        }
    }

    suspend fun users(): List<UserDto> {
        return newSuspendedTransaction {
            UserDao.all().toDto()
        }
    }

}


class CityQueryService : Query {
    @Authenticated
    fun nested() = Nested()

    suspend fun citiesDao(dataFetchingEnvironment: DataFetchingEnvironment): List<CityDto> {
        val selectedFields = dataFetchingEnvironment.selectedFields()
        return newSuspendedTransaction {
            val cities = selectedFields.whenField(
                CityDto::users,
                { CityDao.all().with(CityDao::users) },
                { CityDao.all() }
            )

            cities.map { city ->
                CityDto(
                    name = city.name,
                    users = selectedFields.whenField(
                        CityDto::users,
                        { city.users.toDto() },
                        { emptyList() },
                    )
                )
            }
        }
    }

    suspend fun citiesLazyDto(dataFetchingEnvironment: DataFetchingEnvironment): List<CityLazyDto> {
        return newSuspendedTransaction {
            val cities = CityDao.all()
            cities.map { city ->
                CityLazyDto(
                    id = city.id.value,
                    name = city.name,
                )
            }
        }
    }

    @GraphQLDescription("List of cities")
    suspend fun citiesDsl(dataFetchingEnvironment: DataFetchingEnvironment): List<CityDto> {
        val selectedFields = dataFetchingEnvironment.selectedFields()
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
                                email = row[Users.email],
                                role = row[Users.role],
                            )
                        )
                    } else emptyList(),
                )
            }
        }
    }
}
