package dev.xanter.graphql.dataloaders

import dev.xanter.models.UserDto
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory

object UserDataLoader : CoroutineKotlinDataLoader<Int, UserDto?>() {
    override val dataLoaderName = "USER_LOADER"
    override fun getDataLoader(): DataLoader<Int, UserDto?> = DataLoaderFactory.newDataLoader<Int, UserDto?> { ids ->
        scope.future {
            UserDto.search(ids)
        }
    }
}
