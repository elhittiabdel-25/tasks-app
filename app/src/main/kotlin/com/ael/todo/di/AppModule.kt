package com.ael.todo.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkManager
import com.ael.todo.data.db.AppDatabase
import com.ael.todo.data.db.dao.CategoryDao
import com.ael.todo.data.db.dao.CategoryKeywordDao
import com.ael.todo.data.db.dao.SubtaskDao
import com.ael.todo.data.db.dao.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.getInstance(ctx)

    @Provides @Singleton
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides @Singleton
    fun provideSubtaskDao(db: AppDatabase): SubtaskDao = db.subtaskDao()

    @Provides @Singleton
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides @Singleton
    fun provideCategoryKeywordDao(db: AppDatabase): CategoryKeywordDao = db.categoryKeywordDao()
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {

    @Provides @Singleton
    fun provideWorkManagerConfiguration(
        workerFactory: androidx.hilt.work.HiltWorkerFactory
    ): Configuration = Configuration.Builder()
        .setWorkerFactory(workerFactory)
        .build()
}
