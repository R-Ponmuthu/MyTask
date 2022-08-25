package com.example.task.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picture_table")
data class Picture(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val path: String,
)