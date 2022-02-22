package com.jack.beautyeffect

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jack.beautyeffect.databinding.ActivityCameraBinding
import java.lang.Exception

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding

    private var preview: Preview? = null
    private var imageCaputre: ImageCapture? = null
    private var ImageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private lateinit var viewFinder: PreviewView
    private var lensFacing = CameraSelector.LENS_FACING_FRONT

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

        val camera_capture_btn = binding.cameraCaptureBtn

        camera_capture_btn.setOnClickListener {
            takePhoto()
        }

    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            // preview
            preview = Preview.Builder()
                .build()

            // camera switch btn (not yet implemented)
//            val cameraSwitchBtn = binding.cameraSwitchBtn
//            cameraSwitchBtn.setOnClickListener {
//                lensFacing = if(CameraSelector.LENS_FACING_FRONT == lensFacing) {
//                    CameraSelector.LENS_FACING_BACK
//                } else {
//                    CameraSelector.LENS_FACING_FRONT
//                }
//                Log.d(TAG, "startCamera: clicked")
//            }
            // select camera(front or back)
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)

                preview?.setSurfaceProvider(viewFinder.surfaceProvider)

            } catch (exc: Exception) {
                Log.e(TAG, "Case binding failed")
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {

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
        private const val permisionRC = 100
        private val permissions = listOf(Manifest.permission.CAMERA)
    }
}