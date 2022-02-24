package com.jack.beautyeffect.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jack.beautyeffect.BitmapUtils
import com.jack.beautyeffect.databinding.ActivityCameraBinding
import com.kmint.alanfacem.ai.FaceDetector
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

    private val cameraExecutor  = Executors.newSingleThreadExecutor()


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCameraBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewFinder = binding.viewFinder
        if (!hasPermissions(this)) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), permisionRC)
        }
        else {
            startCamera()
        }

         // camera switch btn
        val cameraSwitchBtn = binding.cameraSwitchBtn
        cameraSwitchBtn.setOnClickListener {
            if(lensFacing == CameraSelector.LENS_FACING_FRONT) {
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

        val camera_capture_btn = binding.cameraCaptureBtn

        camera_capture_btn.setOnClickListener {
            takePhoto()
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        faceDetector = FaceDetector()
        val faceNumInfo = binding.faceNumInfo

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            // preview
            preview = Preview.Builder()
                .build()

            // image analysis
            imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            imageAnalyzer!!.setAnalyzer(cameraExecutor,  { image ->
                val bitmap = BitmapUtils.imageToBitmap(image.image!!, image.imageInfo.rotationDegrees)

                faceDetector.detect(image) { faces ->
                    faceNumInfo.setText("FaceNum: ${faces.size}")
                    if (faces.size > 0)
                        faceNumInfo.setTextColor(Color.RED)
                    else
                        faceNumInfo.setTextColor(Color.BLACK)

                    faces.forEach { face->
                        // get face bounding box
                        val faceBox = face.boundingBox
                        val landmarks = face.allLandmarks
                        Log.d(TAG, "BoundingBox: " + faceBox)
                        Log.d(TAG, "Landmarks: " + landmarks)
                    }

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
        private const val permisionRC = 100
        private val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}