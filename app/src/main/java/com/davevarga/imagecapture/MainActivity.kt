package com.davevarga.imagecapture

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {


    private val REQUEST_MULTIPLE_PERMISSIONS = 100
    private var imageView: ImageView? = null
    private var expandedImageView: ImageView? = null
    private lateinit var currentPhotoPath: String
    private lateinit var photoFile: File


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        imageView = capturedImage
        expandedImageView = expanded_image
        val permissions = listOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val photoButtonAction = photoButton

        photoButtonAction.setOnClickListener {
            if (!hasPermissions(this, permissions[0], permissions[1])) {
                requestPermissions(
                    arrayOf(permissions[0], permissions[1]),
                    REQUEST_MULTIPLE_PERMISSIONS
                )
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    dispatchTakePictureIntent()
                }
            }
        }

        imageView?.setOnClickListener {
            if (it != null)
                expandedImageView?.visibility = (View.VISIBLE)
        }

        expandedImageView?.setOnClickListener {
            if (it != null)
                expandedImageView?.visibility = (View.INVISIBLE)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_MULTIPLE_PERMISSIONS ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "permissions granted", Toast.LENGTH_SHORT).show()
                    dispatchTakePictureIntent()
                } else {
                    Toast.makeText(this, "permissions denied", Toast.LENGTH_SHORT).show()
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MULTIPLE_PERMISSIONS && resultCode == Activity.RESULT_OK) {

            val myBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            imageView?.setImageBitmap(myBitmap)
            expandedImageView?.setImageBitmap(myBitmap)

        }
    }

    fun hasPermissions(context: Context, vararg permissions: String): Boolean = permissions.all {
        ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                photoFile = createImageFile()
                photoFile.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_MULTIPLE_PERMISSIONS)
                }
            }
        }
    }

}
