package dev.xanter.graphql

import com.expediagroup.graphql.generator.hooks.FlowSubscriptionSchemaGeneratorHooks
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.plugin.schema.hooks.SchemaGeneratorHooksProvider

class KtorSchemaGenerationHooksProvider: SchemaGeneratorHooksProvider {
    override fun hooks(): SchemaGeneratorHooks = FlowSubscriptionSchemaGeneratorHooks()

}
