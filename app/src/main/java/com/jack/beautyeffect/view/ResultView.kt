package com.jack.beautyeffect.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.core.graphics.get
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.face.Face
import com.jack.beautyeffect.beautyUtils.ResurfacingUtils
import com.jack.beautyeffect.beautyUtils.SmallFaceUtils
import com.jack.beautyeffect.beautyUtils.WhiteningUtils
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import java.util.LinkedHashMap

class ResultView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var frameWidth = 0f
    private var frameHeight = 0f
    private var xFactor = 0f
    private var yFactor = 0f

    private var lensFacing = 0
    private var faces = listOf<Face>()
    private var bitmap: Bitmap? = null
    private var functions = mutableMapOf<String, Int>()

    private val facePaint = Paint()
    private var faceBoxF = RectF()
    private lateinit var transformMatrix: Matrix

    private lateinit var resultBitmap: Bitmap

    var frameSize = Size(0, 0)

    init {
        facePaint.color = Color.RED
        facePaint.style = Paint.Style.STROKE
        facePaint.strokeWidth = 5.0f
        setWillNotDraw(false)
    }

    fun updateFaces(faces: List<Face>, lensFacing: Int, bitmap: Bitmap, functions: MutableMap<String, Int>) {
        this.faces = faces
        this.lensFacing = lensFacing
        this.bitmap = bitmap
        this.functions = functions
        transformMatrix = Matrix()
        invalidate()
    }

    // In front camera situation, the detection result coordinates will be mirror, we need to reverse these coordinates
    private fun translateX(x: Float) = if (lensFacing == CameraSelector.LENS_FACING_FRONT) (bitmap!!.width - x) * xFactor
                                        else x * xFactor
    private fun translateY(y: Float) = y * yFactor

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            // canvas size equals to view size
            viewWidth = width.toFloat()
            viewHeight = height.toFloat()
            frameWidth = frameSize.width.toFloat()
            frameHeight = frameSize.height.toFloat()
            Log.d(TAG, "View size: " + viewHeight + " " + viewWidth) // keep H:W = 4:3

            // calculate factor between view size and frame size to scale
            xFactor = viewWidth / frameWidth
            yFactor = viewHeight / frameHeight

            Log.d(TAG, "Scale factor: " + xFactor + " " + yFactor)
            // maybe need to add one condition: only do small face when only one face detected
            faces.forEach { face ->
                faceBoxF = face.boundingBox.toRectF()

                // need to check if contours have been detected
                if (face.allContours.size > 11) {
                    val faceOval = face.allContours[0].points
                    val noseBridge = face.allContours[11].points

                    if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                        transformMatrix.postScale(-1.0f, 1.0f)
                    else
                        transformMatrix.postScale(1.0f, 1.0f)

                    // org bitmap
//                    bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, transformMatrix, false)
//                    canvas.drawBitmap(bitmap!!, 0f, 0f, facePaint)

                    // result bitmap

                    resultBitmap = SmallFaceUtils().smallFace(bitmap!!, faceOval, noseBridge, functions["瘦臉"]!!)
                    resultBitmap = ResurfacingUtils().resurface(resultBitmap, context, functions["磨皮"]!!)
                    resultBitmap = WhiteningUtils().whitening(resultBitmap, context, functions["美白"]!!)

                    transformMatrix.postScale(xFactor, yFactor) // // scale to view size
                    resultBitmap = Bitmap.createBitmap(
                        resultBitmap,
                        0,
                        0,
                        resultBitmap.width,
                        resultBitmap.height,
                        transformMatrix,
                        false
                    )
                    canvas.drawBitmap(resultBitmap, 0f,0f, null)

                }

                // draw face result
//                faceBoxF.set(
//                    translateX(faceBoxF.left),
//                    translateY(faceBoxF.top),
//                    translateX(faceBoxF.right),
//                    translateY(faceBoxF.bottom)
//                )
//
//                canvas.drawRect(faceBoxF, facePaint)
//
//                face.allContours.forEach {
//                    it.points.forEach {
//                        canvas.drawCircle(translateX(it.x), translateY(it.y), 3f, facePaint)
//                    }
//                }
            }
        }
    }
    companion object {
        private val TAG = ResultView::class.java.simpleName
    }

}