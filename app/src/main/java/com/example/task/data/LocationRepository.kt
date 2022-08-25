package com.example.task.data

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.annotation.WorkerThread
import com.example.task.TaskApplication
import com.example.task.data.db.LocationDao
import com.example.task.model.Location
import com.google.android.gms.location.LocationServices
import javax.inject.Inject

class LocationRepository @Inject constructor(
    private val locationDao: LocationDao
) {

    fun getLocations() = locationDao.getLocations()

    @WorkerThread
    suspend fun updateLocation(location: Location) {
        locationDao.updateLocation(location)
    }
}
