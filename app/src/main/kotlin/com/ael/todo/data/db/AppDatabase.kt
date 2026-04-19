package com.ael.todo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ael.todo.data.db.dao.CategoryDao
import com.ael.todo.data.db.dao.CategoryKeywordDao
import com.ael.todo.data.db.dao.SubtaskDao
import com.ael.todo.data.db.dao.TaskDao
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.entity.CategoryKeyword
import com.ael.todo.data.db.entity.Subtask
import com.ael.todo.data.db.entity.Task

@Database(
    entities = [Task::class, Subtask::class, Category::class, CategoryKeyword::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun categoryKeywordDao(): CategoryKeywordDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tasks.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Seed categories — Personal first so it gets id=1 (FK default)
            val cats = listOf(
                "Personal" to 0xFF9E9E9E.toInt(),
                "Shopping" to 0xFF9C27B0.toInt(),
                "Work" to 0xFF2196F3.toInt(),
                "Health" to 0xFF4CAF50.toInt(),
                "Home" to 0xFFFF9800.toInt(),
                "Finance" to 0xFF009688.toInt()
            )
            cats.forEach { (name, color) ->
                db.execSQL(
                    "INSERT INTO categories (name, color, is_predefined) VALUES (?, ?, 1)",
                    arrayOf(name, color)
                )
            }
            // Seed keywords: categoryId matches insert order (1=Personal…6=Finance)
            val keywords = mapOf(
                2L to listOf("buy", "shop", "groceries", "grocery", "purchase", "order", "pickup"),
                3L to listOf("email", "call", "meeting", "report", "send", "submit", "deadline", "review"),
                4L to listOf("doctor", "gym", "workout", "medicine", "appointment", "dentist", "run"),
                5L to listOf("clean", "laundry", "dishes", "repair", "fix", "vacuum", "trash"),
                6L to listOf("pay", "bill", "invoice", "transfer", "budget", "tax")
            )
            keywords.forEach { (catId, kws) ->
                kws.forEach { kw ->
                    db.execSQL(
                        "INSERT INTO category_keywords (category_id, keyword) VALUES (?, ?)",
                        arrayOf(catId, kw.lowercase())
                    )
                }
            }
        }
    }
}
