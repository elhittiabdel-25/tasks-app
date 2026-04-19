package com.ael.todo.domain

import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.CategoryKeyword
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryMatcher @Inject constructor() {

    fun match(
        title: String,
        keywords: List<CategoryKeyword>,
        categories: List<Category>
    ): Category? {
        if (title.isBlank() || keywords.isEmpty()) return categories.firstOrNull { it.name == "Personal" }
        val lower = title.lowercase()
        data class Hit(val categoryId: Long, val position: Int)
        val hits = mutableListOf<Hit>()
        keywords.forEach { kw ->
            val idx = findWordBoundary(lower, kw.keyword.lowercase())
            if (idx >= 0) hits.add(Hit(kw.categoryId, idx))
        }
        if (hits.isEmpty()) return categories.firstOrNull { it.name == "Personal" }
        val best = hits.minWithOrNull(compareBy({ it.position }, { it.categoryId }))!!
        return categories.firstOrNull { it.id == best.categoryId }
    }

    private fun findWordBoundary(text: String, word: String): Int {
        var start = 0
        while (true) {
            val idx = text.indexOf(word, start)
            if (idx < 0) return -1
            val before = idx == 0 || !text[idx - 1].isLetterOrDigit()
            val after = idx + word.length == text.length || !text[idx + word.length].isLetterOrDigit()
            if (before && after) return idx
            start = idx + 1
        }
    }
}
