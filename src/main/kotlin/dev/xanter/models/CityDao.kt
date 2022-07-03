package dev.xanter.models

import com.expediagroup.graphql.server.extensions.getValuesFromDataLoader
import dev.xanter.graphql.dataloaders.UserDataLoader
import graphql.schema.DataFetchingEnvironment
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.concurrent.CompletableFuture

object Cities : IntIdTable() {
    val name = varchar("name", 50)
}

class CityDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CityDao>(Cities)

    var name by Cities.name
    val users by UserDao referrersOn Users.city
}

data class CityDto(
    val name: String,
    val users: List<UserDto>,
)

data class CityLazyDto(
    val id: Int,
    val name: String,
) {
    suspend fun users(dataFetchingEnvironment: DataFetchingEnvironment): List<UserDto> {
        return newSuspendedTransaction {
            UserDao.find {
                Users.city eq this@CityLazyDto.id
            }.toDto()
        }
    }
}

data class CityLoaderDto(
    val id: Int,
    val name: String,
) {
    fun users(dataFetchingEnvironment: DataFetchingEnvironment): CompletableFuture<List<UserDto>> {
        return dataFetchingEnvironment.getValuesFromDataLoader(UserDataLoader.dataLoaderName, emptyList<Int>())
    }

    companion object {
        suspend fun search(ids: List<Int> = emptyList()): List<CityLoaderDto> {
            return newSuspendedTransaction {
                if (ids.isEmpty()) {
                    CityDao.all()
                } else {
                    CityDao.find {
                        Cities.id inList ids
                    }
                }.map {
                    CityLoaderDto(
                        id = it.id.value,
                        name = it.name,
                    )
                }
            }
        }
    }
}
