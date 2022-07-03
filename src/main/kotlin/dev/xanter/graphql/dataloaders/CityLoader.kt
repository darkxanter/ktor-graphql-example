package dev.xanter.graphql.dataloaders

import dev.xanter.models.CityLoaderDto
import kotlinx.coroutines.future.future
import org.dataloader.DataLoader
import org.dataloader.DataLoaderFactory

object CityDataLoader : CoroutineKotlinDataLoader<Int, CityLoaderDto?>() {
    override val dataLoaderName = "CITY_LOADER"
    override fun getDataLoader(): DataLoader<Int, CityLoaderDto?> = DataLoaderFactory.newDataLoader<Int, CityLoaderDto?> { ids ->
        scope.future {
            CityLoaderDto.search(ids)
        }
    }
}
