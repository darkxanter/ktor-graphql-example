package dev.xanter.graphql

import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import org.dataloader.DataLoaderRegistry

/**
 * Example of how to register the various DataLoaders using [DataLoaderRegistryFactory]
 */
class KtorDataLoaderRegistryFactory : DataLoaderRegistryFactory {

    override fun generate(): DataLoaderRegistry {
        val registry = DataLoaderRegistry()
//        registry.register(UniversityDataLoader.dataLoaderName, UniversityDataLoader.getDataLoader())
//        registry.register(CourseDataLoader.dataLoaderName, CourseDataLoader.getDataLoader())
//        registry.register(BookDataLoader.dataLoaderName, BookDataLoader.getDataLoader())
        return registry
    }
}
