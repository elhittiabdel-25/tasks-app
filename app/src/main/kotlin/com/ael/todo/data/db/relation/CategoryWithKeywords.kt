package com.ael.todo.data.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.CategoryKeyword

data class CategoryWithKeywords(
    @Embedded val category: Category,
    @Relation(parentColumn = "id", entityColumn = "category_id")
    val keywords: List<CategoryKeyword>
)
