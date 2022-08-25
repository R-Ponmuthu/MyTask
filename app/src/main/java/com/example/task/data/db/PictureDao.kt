package com.example.task.data.db

import androidx.room.*
import com.example.task.model.Location
import com.example.task.model.Picture
import kotlinx.coroutines.flow.Flow

@Dao
interface PictureDao {

    @Transaction
    suspend fun savePicture(picture: Picture) {
        picture.let {
            insertPicture(it)
        }
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPicture(picture: Picture)

    @Query("SELECT * FROM picture_table")
    fun getPictures(): Flow<List<Picture>>
}
