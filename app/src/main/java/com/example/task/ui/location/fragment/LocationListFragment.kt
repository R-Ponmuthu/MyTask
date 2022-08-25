package com.example.task.ui.fragment

import android.Manifest
import android.app.Application
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.task.*
import com.example.task.data.LocationRepository
import com.example.task.databinding.LocationListFragmentBinding
import com.example.task.service.ForegroundOnlyLocationService
import com.example.task.ui.location.adapter.LocationAdapter
import com.example.task.utils.SharedPreferenceUtil
import com.example.task.viewmodel.LocationListViewModel
import com.example.task.workmanager.TrackLocationWorker
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "LocationListFragment"
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34

@AndroidEntryPoint
class LocationListFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: LocationListViewModel by hiltNavGraphViewModels(R.id.navGraph)

    @Inject
    lateinit var repository: LocationRepository
    private lateinit var adapter: LocationAdapter
    private var foregroundOnlyLocationServiceBound = false
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: LocationListFragmentBinding
//        get() = _binding!!

    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override
    fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LocationListFragmentBinding.inflate(inflater, container, false)

        init()

        return binding.root
    }


    private fun init() {

        sharedPreferences = activity!!.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

//        CoroutineScope(Dispatchers.IO).launch { viewModel.getSavedLocation() }

//        binding.locationButton.setOnClickListener {
//            getFromLocation()
//        }

        binding.locationButton.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
            )

            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                if (foregroundPermissionApproved()) {
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                        ?: Log.d(TAG, "Service Not Bound")
                } else {
                    requestForegroundPermissions()
                }
            }
        }

        adapter = LocationAdapter(listOf())
        binding.locationList.adapter = adapter

        repository.getLocations()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it.isNotEmpty()) {
                    adapter.locations = it
                }
            }
            .launchIn(lifecycleScope)

//        viewModel.location.observe(viewLifecycleOwner) {
//            adapter.locations = it
//        }
    }

    private fun navigateToSettings() {
//        findNavController().navigate(
//            ArticleListFragmentDirections
//                .actionArticleListFragmentToSettingsFragment()
//        )
    }

    fun getFromLocation() =
        if (isGPSEnabled()) trackLocation() else locationSetup()

    fun isGPSEnabled() =
        (activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
            LocationManager.GPS_PROVIDER
        )

    private fun locationSetup() {
        LocationServices.getSettingsClient(Application())
            .checkLocationSettings(
                LocationSettingsRequest.Builder()
                    .addLocationRequest(LocationRequest())
                    .setAlwaysShow(true)
                    .build()
            )
            .addOnSuccessListener { true }
            .addOnFailureListener {
            }
    }

    private fun trackLocation() {
        val locationWorker =
            PeriodicWorkRequestBuilder<TrackLocationWorker>(15, TimeUnit.MINUTES).addTag(
                LocationListViewModel.LOCATION_WORK_TAG
            ).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(
            LocationListViewModel.LOCATION_WORK_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            locationWorker
        )
    }

    private fun foregroundPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun requestForegroundPermissions() {
        val provideRationale = foregroundPermissionApproved()

        // If the user denied a previous request, but didn't check "Don't ask again", provide
        // additional rationale.
        if (provideRationale) {
            Snackbar.make(
                binding.rootMain,
                R.string.permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(
                TAG,
                "Request foreground only permission"
            )
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            binding.locationButton.text =
                getString(R.string.stop_location_updates_button_text)
        } else {
            binding.locationButton.text =
                getString(R.string.start_location_updates_button_text)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(
                        TAG,
                        "User interaction was cancelled."
                    )
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    // Permission was granted.
                    foregroundOnlyLocationService?.subscribeToLocationUpdates()
                else -> {
                    // Permission denied.
                    updateButtonState(false)

                    Snackbar.make(
                        binding.rootMain,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    )
                        .setAction(R.string.settings) {
                            // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID,
                                null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val serviceIntent = Intent(activity, ForegroundOnlyLocationService::class.java)
        activity?.bindService(
            serviceIntent,
            foregroundOnlyServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            activity?.unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)

        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            sharedPreferences?.let {
                updateButtonState(
                    it.getBoolean(
                        SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
                    )
                )
            }
        }
    }
}