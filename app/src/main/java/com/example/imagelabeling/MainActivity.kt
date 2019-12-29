package com.example.imagelabeling

import ai.fritz.core.Fritz
import android.os.Bundle
import android.util.Size
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val apikey = BuildConfig.ApiKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize fritz sdk
        Fritz.configure(this, apikey)

//        view_finder.post {
//            startCamera()
//        }
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
    }
}
