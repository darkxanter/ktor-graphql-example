package dev.xanter.graphql.dataloaders

import com.expediagroup.graphql.server.execution.KotlinDataLoader
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

abstract class CoroutineKotlinDataLoader<K, V> : KotlinDataLoader<K, V> {
    protected val scope = CoroutineScope(Dispatchers.IO + SupervisorJob() + CoroutineName("CoroutineKotlinDataLoader"))
}