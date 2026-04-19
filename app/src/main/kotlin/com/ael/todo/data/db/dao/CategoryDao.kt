package com.ael.todo.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.relation.CategoryWithKeywords
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY id ASC")
    suspend fun getAllSync(): List<Category>

    @Transaction
    @Query("SELECT * FROM categories ORDER BY id ASC")
    fun getAllWithKeywords(): Flow<List<CategoryWithKeywords>>

    @Transaction
    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryWithKeywords(id: Long): Flow<CategoryWithKeywords>

    @Query("SELECT * FROM categories WHERE name = 'Personal' LIMIT 1")
    suspend fun getPersonalCategory(): Category?
}
