package com.example.task.data

import androidx.annotation.WorkerThread
import com.example.task.data.db.LocationDao
import com.example.task.model.Location
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
