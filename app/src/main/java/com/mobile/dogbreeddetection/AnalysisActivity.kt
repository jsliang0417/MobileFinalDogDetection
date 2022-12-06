package com.mobile.dogbreeddetection

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView

class AnalysisActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var photoPath: String = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        imageView = findViewById(R.id.analysisImage)

        photoPath = intent.getStringExtra("URI").toString()

        setImage(photoPath)
        Log.d("photoPath in analysis", photoPath)
    }





    private fun setImage(locationImageURI: String) {

        val imageViewWidth: Int = 1000
        val imageViewHeight: Int = 1000

        //obtain image dimension
        val bitMapOptions = BitmapFactory.Options()
        bitMapOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(locationImageURI, bitMapOptions)
        val imageWidth: Int = bitMapOptions.outWidth
        val imageHeight: Int = bitMapOptions.outHeight
        //println("Image View Width: $imageViewWidth, Image View Height: $imageViewHeight")
        //scale down the image to fit in image view
        val scaleFactor =
            Math.max(1, Math.min(imageWidth / imageViewWidth, imageHeight / imageViewHeight))
        //Decode image file into a Bitmap sized to fill the view
        bitMapOptions.inJustDecodeBounds = false
        bitMapOptions.inSampleSize = scaleFactor
        var bitMap = BitmapFactory.decodeFile(locationImageURI, bitMapOptions)
        bitMap = determineAngle(locationImageURI, bitMap)
        imageView.setImageBitmap(bitMap)
    }

    private fun determineAngle(uri: String, bitMap: Bitmap): Bitmap {
        val ei = ExifInterface(uri)
        val orientation: Int = ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        var rotatedBitmap: Bitmap? = null
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatedBitmap = fixOrientation(bitMap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatedBitmap = fixOrientation(bitMap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatedBitmap = fixOrientation(bitMap, 270)
            ExifInterface.ORIENTATION_NORMAL -> rotatedBitmap = bitMap
            else -> rotatedBitmap = bitMap
        }

        return rotatedBitmap
    }

    //fix image orientation issue
    private fun fixOrientation(bitMap: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(bitMap, 0, 0, bitMap.width, bitMap.height, matrix, true)
    }

}