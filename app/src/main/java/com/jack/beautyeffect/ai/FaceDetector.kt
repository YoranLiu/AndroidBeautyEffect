package com.kmint.alanfacem.ai

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions


class FaceDetector {
    private var faceOptions: FaceDetectorOptions = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST) // Accuracy or Fast mode
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    private var detector: FaceDetector = FaceDetection.getClient(faceOptions)

    @SuppressLint("UnsafeOptInUsageError")
    fun detect(image: ImageProxy, callback: (List<Face>) -> Unit) {

        detector.process(InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees))
            .addOnSuccessListener { faces ->
                Log.d(TAG, "Number of faces: ${faces.size}")
                callback(faces)
            }
            .addOnFailureListener {
                Log.e(TAG, "Detection failed: ${it.stackTrace}")
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    companion object {
        private val TAG = FaceDetector::class.java.simpleName
    }
}