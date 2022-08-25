package com.example.task.data

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.task.TaskApplication
import com.example.task.data.db.LocationDao
import com.example.task.data.db.PictureDao
import com.example.task.model.Location
import com.example.task.model.Picture
import com.google.android.gms.location.LocationServices
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
