package com.jack.beautyeffect.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.jack.beautyeffect.BitmapUtils
import com.jack.beautyeffect.databinding.ActivityCameraBinding
import com.jack.beautyeffect.view.ResultView
import com.kmint.alanfacem.ai.FaceDetector
import org.jetbrains.anko.tableLayout
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private lateinit var faceDetector: FaceDetector

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

    private lateinit var viewFinder: PreviewView
    private lateinit var resultView: ResultView
    private lateinit var seekBar: SeekBar
    private lateinit var barStrength: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var bitmap: Bitmap

    private var faceStrengthFactor = 0
    private var functionIdx = 0
    private val cameraExecutor  = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewFinder = binding.viewFinder
        resultView = binding.resultView
        seekBar = binding.seekBar
        barStrength = binding.barStrength
        tabLayout = binding.tabLayout

        for (item in beautyFunctions) {
            tabLayout.addTab(tabLayout.newTab().setText(item.key))
        }

        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                functionIdx = tab!!.position

                Log.d(TAG, "onTabSelected: " + tab.text)
                //seekBar.progress = beautyFunctions[tab.]
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                barStrength.text = progress.toString()
                faceStrengthFactor = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                barStrength.visibility = View.VISIBLE
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                barStrength.visibility = View.INVISIBLE
            }
        })


        if (!hasPermissions(this)) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), permisionRC)
        }
        else {
            startCamera()
        }

         // camera switch btn
        val cameraSwitchBtn = binding.cameraSwitchBtn
        cameraSwitchBtn.setOnClickListener {
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                lensFacing = CameraSelector.LENS_FACING_BACK
                Toast.makeText(this, "Switch to rear camera", Toast.LENGTH_SHORT)
                    .show()
            }
            else {
                lensFacing = CameraSelector.LENS_FACING_FRONT
                Toast.makeText(this, "Switch to front camera", Toast.LENGTH_SHORT)
                    .show()
            }
            startCamera()
        }

//        val camera_capture_btn = binding.cameraCaptureBtn

//        camera_capture_btn.setOnClickListener {
//            takePhoto()

//            if (bitmap != null) {
//
//                MediaStore.Images.Media.insertImage(contentResolver, bitmap, "title", "description")
//                Toast.makeText(this, "saved", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }
    }


    private fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        Log.d(TAG, "lensFacing: " + lensFacing)
        faceDetector = FaceDetector()


        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            // preview
            preview = Preview.Builder()
                .build()

            // image analysis
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imageAnalyzer!!.setAnalyzer(cameraExecutor,  { image ->
                // front camera: 270 degrees, back camera: 90 degrees
                Log.d(TAG, "RotationDegree: " + image.imageInfo.rotationDegrees)
                Log.d(TAG, "Camera resolution: " + image.width + " " + image.height)
                bitmap = BitmapUtils.imageToBitmap(image.image!!, image.imageInfo.rotationDegrees)

                // original W x H: 640 x 480, need to flip to 480 x 640
                resultView!!.frameSize = Size(bitmap.width, bitmap.height) // 480 x 640
                 Log.d(TAG, "Image info: ${bitmap.width} ${bitmap.height}")

                faceDetector.detect(image) { faces ->
                    resultView!!.updateFaces(faces, lensFacing, bitmap, faceStrengthFactor)
                }
            })

            // image capture
            imageCapture = ImageCapture.Builder()
                .build()
            // select camera(front or back)
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer,
                    imageCapture
                )

                preview?.setSurfaceProvider(viewFinder.surfaceProvider)


            } catch (exc: Exception) {
                Log.e(TAG, "Case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        // if imageCapure is null(click take photo button before starting camera), then return
        val imageCapture = imageCapture?: return

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME,
                FILENAME_PREFIX + SimpleDateFormat(FILENAME_FORMAT).format(System.currentTimeMillis()))
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object: ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo Saved: " + outputFileResults.savedUri?.path)
                    Toast.makeText(baseContext, "Photo saved", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permisionRC && hasPermissions(this)) {
            startCamera()
            Log.d(TAG, "onCreate: permission granted")
        }
        else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG)
                .show()
            finish()
        }
    }

    // check all permissions have been granted
    private fun hasPermissions(context: Context) = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private val TAG = CameraActivity::class.java.simpleName
        private const val FILENAME_PREFIX = "beauty_"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"
        private val beautyFunctions = mapOf("瘦臉" to 0, "磨皮" to 0, "美白" to 0) // key: function name; value: seekbar value(default set to 0)
        private const val permisionRC = 100
        private val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}