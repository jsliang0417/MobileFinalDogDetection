package com.mobile.dogbreeddetection

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    //component variables


    //variables
    var currentPhotoPath: String = ""



    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MapsActivity", "Permission Granted")
            } else {
//                Toast.makeText(
//                    this,
//                    "Location Permissions not granted. Location disabled on map",
//                    Toast.LENGTH_LONG
//                ).show()
            }
        }

    private val takePictureResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(applicationContext, "No picture taken", Toast.LENGTH_LONG)
            } else {
                Log.d("MainActivity", "Picture Taken at location $currentPhotoPath")
                val intent = Intent(this, AnalysisActivity::class.java)
                intent.putExtra("URI", currentPhotoPath)
                startActivity(intent)
            }
        }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //init variables

        checkForCameraPermission()
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            takeNewPhoto()
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    private fun takeNewPhoto() {
        val cameraIntent = Intent().setAction(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            val filePath: String = createFilePath()
            val myFile: File = File(filePath)
            currentPhotoPath = filePath
            val photoURI = FileProvider.getUriForFile(this, "com.mobile.dogbreeddetection.fileprovider", myFile)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            takePictureResultLauncher.launch(cameraIntent)
        }
    }

    private fun createFilePath(): String {
        val timeStamp = SimpleDateFormat("yyyyMMDD_HHmmss", Locale.US).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(imageFileName, ".jpg", storageDir)
        return image.absolutePath
    }


    private fun checkForCameraPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                println("Camera Permission Granted")
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}