package com.example.task.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.task.data.LocationRepository
import com.example.task.utils.toLocation
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TrackLocationWorker(context: Context, wp: WorkerParameters) :
    Worker(context, wp) {

    private val LOCATION_UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(1)
    private val LOCATION_FASTEST_UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(1)

    private var ctx: Context = context

    @Inject
    lateinit var repository: LocationRepository

    override fun doWork(): Result {
        return if (getLocationUpdate()) {
            Log.d("MyWorker", "SUCCESS")
            Result.success()
        } else {
            Log.d("MyWorker", "FAILURE")
            Result.failure()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocationUpdate(): Boolean {

        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                super.onLocationResult(result)

                result?.lastLocation.toLocation().let {
                    CoroutineScope(Dispatchers.IO).launch {
                        it?.let { it1 -> repository.updateLocation(it1) }
                    }
                }

//                result?.locations?.let { locations->
//                    for (location in locations) {
//                        Log.d("MyWorker", "NEW Location: $location")
//                    }
//                }
            }
        }

        val locationRequest = LocationRequest().apply {
            interval = LOCATION_UPDATE_INTERVAL
            fastestInterval = LOCATION_FASTEST_UPDATE_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val result = fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d("LocationWorker", result.exception?.message.toString())
        Log.d("LocationWorker", result.exception?.cause.toString())
        Log.d("LocationWorker", result.exception.toString())
        return result.isSuccessful
    }
}

//class TrackLocationWorker @Inject constructor(
//    context: Context,
//    workerParams: WorkerParameters
//) : Worker(context, workerParams) {
//
//    @Inject
//    lateinit var repository: LocationRepository
//
//    init {
//
//    }
//
//    override fun doWork(): Result {
//        return try {
//            repository.getLocations()
//            Result.success()
//        } catch (e: Exception) {
//            e.message?.let { Log.e("Exception", it) }
//            Result.failure()
//        }
//    }
//}