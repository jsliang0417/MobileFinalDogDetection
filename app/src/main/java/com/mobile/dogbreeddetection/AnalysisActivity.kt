package com.mobile.dogbreeddetection

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mobile.dogbreeddetection.RetrofitAPI.RetrofitDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text
import retrofit2.HttpException
import java.io.ByteArrayOutputStream


class AnalysisActivity : AppCompatActivity() {


    //retrofit
    private lateinit var dogBreedResource: RetrofitDataSource

    //component
    private lateinit var imageView: ImageView
    private lateinit var analyzeButton: Button
    private lateinit var scrollInformation: ScrollView
    private lateinit var dogBreedResult: TextView
    private lateinit var dogBreedConfidenceScore: TextView
    private lateinit var dogBreedType: TextView
    private lateinit var dogBreedDescription: TextView
    private lateinit var dogBreedOrigin: TextView
    private lateinit var dogBreedLifeSpan: TextView
    //private lateinit var loadingCircle: RelativeLayout

    //variable
    private var photoPath: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val analysisActivityActionBar = supportActionBar

        dogBreedResource = RetrofitDataSource(applicationContext)

        imageView = findViewById(R.id.analysisImage)
        analyzeButton = findViewById(R.id.analyze)
        scrollInformation = findViewById(R.id.infomationScrollView)
        dogBreedResult = findViewById(R.id.dog_type)
        dogBreedConfidenceScore = findViewById(R.id.confidence_score)
        dogBreedType = findViewById(R.id.breed_type)
        dogBreedDescription = findViewById(R.id.breed_description)
        dogBreedOrigin = findViewById(R.id.origin)
        dogBreedLifeSpan = findViewById(R.id.life_span)
        //loadingCircle = findViewById(R.id.loadingPanel)


        photoPath = intent.getStringExtra("URI").toString()

        setImage(photoPath)


        analyzeButton.setOnClickListener {
            Toast.makeText(this, "Analyzing...", Toast.LENGTH_LONG).show()
            analyzeButton.text = "Analyzing..."
            val bitmap = convertToBitMap()
            val uri = convertToBase64(bitmap)
            findBreedRetrofit(uri)
            //loadingCircle.visibility = View.VISIBLE;
            Thread.sleep(1500)
            //loadingCircle.visibility = View.INVISIBLE;
            scrollInformation.visibility = View.VISIBLE
            analyzeButton.visibility = View.INVISIBLE
        }

        analysisActivityActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @SuppressLint("SetTextI18n")
    private fun findBreedRetrofit(uri: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val dogBreed = dogBreedResource.getDogBreed(uri)
            withContext(Dispatchers.IO) {
                try {

                    dogBreedResult.text = dogBreed?.dog_type.toString()
                    dogBreedConfidenceScore.text = dogBreed?.confidence_score.toString()
                    dogBreedType.text = dogBreed?.breed_facts?.get(0)?.breedType.toString()
                    dogBreedOrigin.text = dogBreed?.breed_facts?.get(0)?.origin.toString()
                    println("test: ${dogBreed?.breed_facts?.get(0)?.origin.toString()}")
                    val minLifeSpan: String = dogBreed?.breed_facts?.get(0)?.minLifeSpan.toString()
                    val maxLifeSpan: String = dogBreed?.breed_facts?.get(0)?.maxLifeSpan.toString()
                    dogBreedLifeSpan.text = "$minLifeSpan ~ $maxLifeSpan years"
                    dogBreedDescription.text = dogBreed?.breed_facts?.get(0)?.breedDescription.toString()
                    //Log.d("origin", dogBreed?.breed_facts?.get(0)?.origin.toString())
                } catch (e: HttpException) {

                } catch (e: Throwable) {

                }
            }
        }
    }


    private fun convertToBitMap(): Bitmap {
        val drawable = imageView.drawable as BitmapDrawable
        return drawable.bitmap
    }

    private fun getResizedBitmap(image: Bitmap): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = 224
            height = (width / bitmapRatio).toInt()
        } else {
            height = 224
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun convertToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        val newBitmap = getResizedBitmap(bitmap)
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val image = stream.toByteArray()
        return Base64.encodeToString(image, Base64.DEFAULT).replace('/', '_')
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

        bitMapOptions.inJustDecodeBounds = false
        //bitMapOptions.inSampleSize = scaleFactor
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}