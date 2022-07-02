package dev.xanter.usecases

import graphql.language.Field
import graphql.schema.DataFetchingEnvironment


fun DataFetchingEnvironment.selectedFields(): Map<String, List<Field>> = field.selectedFields()

fun Field.selectedFields(): Map<String, List<Field>> {
    return selectionSet.getSelectionsOfType(Field::class.java).selectedFields()
}

fun List<Field>.selectedFields(): Map<String, List<Field>> {
    return associate {
        it.name to (it.selectionSet?.getSelectionsOfType(Field::class.java) ?: emptyList())
    }
}
