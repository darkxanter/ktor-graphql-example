package dev.xanter.usecases

import dev.xanter.models.CityDao.Companion.all
import graphql.language.Field
import graphql.schema.DataFetchingEnvironment
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import kotlin.reflect.KProperty1


typealias SelectedFields = Map<String, List<Field>>

fun DataFetchingEnvironment.selectedFields(): Map<String, List<Field>> = field.selectedFields()

fun Field.selectedFields(): Map<String, List<Field>> {
    return selectionSet.getSelectionsOfType(Field::class.java).selectedFields()
}

fun List<Field>.selectedFields(): Map<String, List<Field>> {
    return associate {
        it.name to (it.selectionSet?.getSelectionsOfType(Field::class.java) ?: emptyList())
    }
}

inline fun <R> SelectedFields.whenField(
    field: KProperty1<*, *>,
    block: () -> R,
    orElse: () -> R,
): R {
    return if (containsKey(field.name)) {
        block()
    } else {
        orElse()
    }
}

inline fun <R> SelectedFields.whenField(
    fieldName: String,
    block: () -> R,
    orElse: () -> R,
): R {
    return if (containsKey(fieldName)) {
        block()
    } else {
        orElse()
    }
}

//fun <T: EntityClass<*, *>> T.preloadFields(selectedFields: SelectedFields): T {
//    val rows = all()
//    table.columns.map { column ->
//        if (column.referee != null) {
//
//        }
//    }
//}
