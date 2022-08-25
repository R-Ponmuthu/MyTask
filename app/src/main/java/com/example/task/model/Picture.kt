package com.example.task.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "picture_table")
@Parcelize
data class Picture(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val path: String,
) : Parcelable