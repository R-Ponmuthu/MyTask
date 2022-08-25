package com.example.task.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.task.model.Location
import com.example.task.model.Picture

private const val DB_NAME = "task_database"

@Database(entities = [Location::class, Picture::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun pictureDao(): PictureDao

    companion object {
        fun create(context: Context): TaskDatabase {

            return Room.databaseBuilder(
                context,
                TaskDatabase::class.java,
                DB_NAME
            ).build()
        }
    }
}
