package com.example.imagelabeling

import ai.fritz.core.Fritz
import ai.fritz.vision.FritzVision
import ai.fritz.vision.FritzVisionImage
import ai.fritz.vision.ImageRotation
import ai.fritz.vision.PredictorStatusListener
import ai.fritz.vision.imagelabeling.FritzVisionLabelPredictor
import ai.fritz.vision.imagelabeling.ImageLabelManagedModelFast
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    val apikey = BuildConfig.ApiKey
    val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize fritz sdk
        Fritz.configure(this, apikey)

        view_finder.post {
            startCamera()
        }
    }

    //Creating and displaying the camera preview
    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder()
            .apply {
                setTargetResolution(Size(1920, 1080))
            }.build()

        //generate a preview
        val preview = Preview(previewConfig)

        //Add a listener to update preview automatically
        preview.setOnPreviewOutputUpdateListener {
            val parent = view_finder.parent as ViewGroup

            //remove the old preview
            parent.removeView(view_finder)
            //add the new preview
            parent.addView(view_finder, 0)
            view_finder.surfaceTexture = it.surfaceTexture
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
        }.build()

        val imageAnalysis = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(executor, ImageProcessor())
        }

        Log.i("ds", "Is it working1??")
        // Make sure to upgrade the appcompat dependency to version 1.1.0 or higher
        CameraX.bindToLifecycle(this, preview, imageAnalysis)
        Log.i("ds", "Is it working2??")
    }


    inner class ImageProcessor : ImageAnalysis.Analyzer {
        var predictor: FritzVisionLabelPredictor? = null
        val TAG = javaClass.simpleName

        override fun analyze(image: ImageProxy?, rotationDegrees: Int) {

            //Handle all the ML logic here
            val mediaImage = image?.image
            val imageRotation = ImageRotation.getFromValue(rotationDegrees)
            val visionImage = FritzVisionImage.fromMediaImage(mediaImage, imageRotation)
            val managedModel = ImageLabelManagedModelFast()

            FritzVision.ImageLabeling.loadPredictor(
                managedModel,
                object : PredictorStatusListener<FritzVisionLabelPredictor> {
                    override fun onPredictorReady(p0: FritzVisionLabelPredictor?) {
                        Log.d(TAG, "Image Labeling predictor is ready")
                        predictor = p0
                    }
                })

            val labelResult = predictor?.predict(visionImage)
            runOnUiThread {
                labelResult?.resultString?.let {
                    val sname = it.split(":")
                    Log.e(TAG, it)
                    Log.e(TAG, sname[0])
                    tv_name.text = sname[0]
                } ?: kotlin.run {
                    tv_name.visibility = TextView.INVISIBLE
                }
            }
        }
    }
}


