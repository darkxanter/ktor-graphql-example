@file:Suppress("unused")

package dev.xanter.graphql.schema

import com.expediagroup.graphql.server.operations.Query
import graphql.language.Field
import graphql.schema.DataFetchingEnvironment

class ArticleQueryService : Query {
    suspend fun articles(dataFetchingEnvironment: DataFetchingEnvironment): List<Article> {

        val selectedFields = dataFetchingEnvironment.selectedFields()

        println("articles selectedFields $selectedFields")

        val articles = (1..5).map { counter ->
            if (selectedFields.containsKey(Article::comments.name)) {
                Article(
                    counter, "title $counter", "content $counter", listOf(
                        ArticleComment("test", "ha-ha")
                    )
                )
            } else {
                Article(counter, "title $counter", "content $counter", emptyList())
            }
        }

        return articles
    }
}


data class Article(
    val id: Int,
    val title: String,
    val content: String,
    val comments: List<ArticleComment>,
)

data class ArticleComment(
    val username: String,
    val content: String,
)

private fun DataFetchingEnvironment.selectedFields(): Map<String, List<Field>> = field.selectedFields()

private fun Field.selectedFields(): Map<String, List<Field>> {
    return selectionSet.getSelectionsOfType(Field::class.java).selectedFields()
}

private fun List<Field>.selectedFields(): Map<String, List<Field>> {
    return associate {
        it.name to (it.selectionSet?.getSelectionsOfType(Field::class.java) ?: emptyList())
    }
}
