package com.example.task.di

import android.content.Context
import com.example.task.data.db.LocationDao
import com.example.task.data.db.TaskDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TaskDatabase =
        TaskDatabase.create(context)

    @Provides
    fun provideDao(database: TaskDatabase): LocationDao {
        return database.locationDao()
    }
}