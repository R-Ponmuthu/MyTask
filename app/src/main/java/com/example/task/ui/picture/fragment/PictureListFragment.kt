package com.example.task.ui.picture.fragment

import android.Manifest
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.task.*
import com.example.task.data.PictureRepository
import com.example.task.databinding.PictureListFragmentBinding
import com.example.task.model.Picture
import com.example.task.ui.picture.adapter.PictureAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.Permission
import javax.inject.Inject


private const val TAG = "PictureListFragment"
private const val REQUEST_STORAGE_PERMISSION_REQUEST_CODE = 3
private const val REQUEST_CAMERA_PERMISSION_REQUEST_CODE = 4
private const val GALLERY = 1
private const val CAMERA = 2

@AndroidEntryPoint
class PictureListFragment : Fragment() {

    @Inject
    lateinit var repository: PictureRepository
    private lateinit var adapter: PictureAdapter
    private lateinit var binding: PictureListFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override
    fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PictureListFragmentBinding.inflate(inflater, container, false)

        init()

        return binding.root
    }


    private fun init() {

        binding.cameraButton.setOnClickListener {

            if (isCameraPermissionApproved()) {
                takePhotoFromCamera()
            } else {
                requestCameraPermission()
            }
        }

        binding.pickButton.setOnClickListener {

            if (isStoragePermissionApproved()) {
                pickImageFromGallery()
            } else {
                requestStoragePermission()
            }
        }

        adapter = PictureAdapter(listOf())
        binding.locationList.adapter = adapter

        repository.getPictures()
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach {
                if (it.isNotEmpty()) {
                    adapter.pictures = it
                }
            }
            .launchIn(lifecycleScope)

        adapter.onItemClick = {
            navigateToPictureFragment(it)
        }
    }

    private fun navigateToPictureFragment(picture: Picture) {

        val action =
            PictureListFragmentDirections.actionPictureListFragmentToPictureFragment(picture)
        findNavController().navigate(action)
    }

    fun pickImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA)
    }

    private fun isStoragePermissionApproved(): Boolean {
//        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
//            requireActivity().applicationContext,
//            Manifest.permission.MANAGE_EXTERNAL_STORAGE
//        )

        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result: Int =
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            val result1: Int =
                ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }

    }

    private fun isCameraPermissionApproved(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            requireActivity().applicationContext,
            Manifest.permission.CAMERA
        )
    }

    private fun requestStoragePermission() {
        val provideRationale = isStoragePermissionApproved()

        if (provideRationale) {
            Snackbar.make(
                binding.rootMain,
                R.string.storage_permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    requestPermissions(
                        arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_PERMISSION_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(
                TAG,
                "Request storage permission"
            )

            if (SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data =
                        Uri.parse(
                            java.lang.String.format(
                                "package:%s",
                                requireActivity().packageName
                            )
                        )
                    startActivityForResult(intent, 2296)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, 2296)
                }
            } else {
                //below android 11
                requestPermissions(
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION_REQUEST_CODE
                )
            }

            requestPermissions(
                arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun requestCameraPermission() {
        val provideRationale = isStoragePermissionApproved()

        if (provideRationale) {
            Snackbar.make(
                binding.rootMain,
                R.string.storage_permission_rationale,
                Snackbar.LENGTH_LONG
            )
                .setAction(R.string.ok) {
                    // Request permission
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(
                TAG,
                "Request storage permission"
            )
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    class ImageStorageManager {
        companion object {
            fun saveToInternalStorage(
                context: Context,
                bitmapImage: Bitmap,
                imageFileName: String
            ): String {
                context.openFileOutput(imageFileName, Context.MODE_PRIVATE).use { fos ->
                    bitmapImage.compress(Bitmap.CompressFormat.PNG, 25, fos)
                }
                return context.filesDir.canonicalPath
            }

            fun getImageFromInternalStorage(context: Context, imageFileName: String): Bitmap? {
                val directory = context.filesDir
                val file = File(directory, imageFileName)
                return BitmapFactory.decodeStream(FileInputStream(file))
            }

            fun deleteImageFromInternalStorage(context: Context, imageFileName: String): Boolean {
                val dir = context.filesDir
                val file = File(dir, imageFileName)
                return file.delete()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data!!.data
                try {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            contentURI
                        )
                    var fileName = System.currentTimeMillis().toString().plus(".jpg")

                    var path = ImageStorageManager.saveToInternalStorage(
                        requireContext(),
                        bitmap,
                        fileName
                    )

                    val picture = Picture(path = path.plus("/").plus(fileName))

                    CoroutineScope(Dispatchers.IO).launch {
                        repository.savePicture(picture)
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap

            var fileName = System.currentTimeMillis().toString().plus(".jpg")

            var path = ImageStorageManager.saveToInternalStorage(
                requireActivity().applicationContext,
                thumbnail,
                fileName
            )

            val picture = Picture(path = path.plus("/").plus(fileName))

            CoroutineScope(Dispatchers.IO).launch {
                repository.savePicture(picture)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionResult")

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(
                        TAG,
                        "User interaction was cancelled."
                    )
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    takePhotoFromCamera()
                else ->
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
            REQUEST_STORAGE_PERMISSION_REQUEST_CODE -> when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive empty arrays.
                    Log.d(
                        TAG,
                        "User interaction was cancelled."
                    )
                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    pickImageFromGallery()
                else ->
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