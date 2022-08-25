package com.example.task.data

import androidx.annotation.WorkerThread
import com.example.task.data.db.PictureDao
import com.example.task.model.Picture
import javax.inject.Inject

class PictureRepository @Inject constructor(
    private val pictureDao: PictureDao
) {

    fun getPictures() = pictureDao.getPictures()

    @WorkerThread
    suspend fun savePicture(picture: Picture) {
        pictureDao.savePicture(picture)
    }
}
