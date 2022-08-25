package com.example.task.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkManager
import com.example.task.data.LocationRepository
import com.example.task.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class LocationListViewModel @Inject constructor(
    private val repository: LocationRepository,
) : ViewModel() {

    companion object {
        const val LOCATION_WORK_TAG = "LOCATION_WORK_TAG"
    }

    val enableLocation: MutableLiveData<Boolean> = MutableLiveData()
    val location: MutableLiveData<Flow<List<Location>>> = MutableLiveData()
    val error: MutableLiveData<Throwable> = MutableLiveData()

    fun stopTrackLocation() {
        WorkManager.getInstance().cancelAllWorkByTag(LOCATION_WORK_TAG)
    }

    fun getSavedLocation() {
        location.postValue(repository.getLocations())
    }
}