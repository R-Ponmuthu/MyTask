package com.example.task.ui.picture.fragment

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import javax.inject.Inject

private const val TAG = "PictureListFragment"
private const val REQUEST_STORAGE_PERMISSION_REQUEST_CODE = 3
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
            if (isStoragePermissionApproved()) {
                takePhotoFromCamera()
            } else {
                requestStoragePermission()
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

        val action = PictureListFragmentDirections.actionPictureListFragmentToPictureFragment(picture)
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
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            context!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
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
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_STORAGE_PERMISSION_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.d(
                TAG,
                "Request storage permission"
            )
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION_REQUEST_CODE
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
                return context.filesDir.absolutePath
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
                        MediaStore.Images.Media.getBitmap(activity!!.contentResolver, contentURI)
                    var path = ImageStorageManager.saveToInternalStorage(
                        context!!,
                        bitmap,
                        System.currentTimeMillis().toString() + ".jpg"
                    )

                    val picture = com.example.task.model.Picture(path = path)

                    CoroutineScope(Dispatchers.IO).launch {
                        repository.savePicture(picture)
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        } else if (requestCode == CAMERA) {
            val thumbnail = data!!.extras!!.get("data") as Bitmap

            var path = ImageStorageManager.saveToInternalStorage(
                context!!,
                thumbnail,
                System.currentTimeMillis().toString() + ".jpg"
            )

            val picture = com.example.task.model.Picture(path = path)

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

        }
    }
}