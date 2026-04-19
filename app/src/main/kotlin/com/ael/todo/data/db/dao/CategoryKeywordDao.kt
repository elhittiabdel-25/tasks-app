package com.ael.todo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ael.todo.data.db.entity.CategoryKeyword

@Dao
interface CategoryKeywordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keyword: CategoryKeyword): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keywords: List<CategoryKeyword>)

    @Delete
    suspend fun delete(keyword: CategoryKeyword)

    @Query("DELETE FROM category_keywords WHERE category_id = :categoryId")
    suspend fun deleteForCategory(categoryId: Long)

    @Query("SELECT * FROM category_keywords ORDER BY category_id ASC")
    suspend fun getAllSync(): List<CategoryKeyword>
}
