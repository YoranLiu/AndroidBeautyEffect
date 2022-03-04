package com.jack.beautyeffect.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.core.graphics.toRectF
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceLandmark

class ResultView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var viewWidth = 0f
    private var viewHeight = 0f
    private var frameWidth = 0f
    private var frameHeight = 0f
    private var xFactor = 0f
    private var yFactor = 0f

    private var lensFacing = 0
    private var faces = listOf<Face>()
    private val boxPaint = Paint()
    private var faceBoxF = RectF()
    var frameSize = Size(0, 0)


    init {
        boxPaint.color = Color.RED
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 10.0f
        setWillNotDraw(false)
    }

    fun updateFaces(faces: List<Face>, lensFacing: Int) {
        this.faces = faces
        this.lensFacing = lensFacing
        invalidate()
    }

    // In front camera situation, the detection result coordinates will be mirror, we need to reverse these coordinates
    private fun translateX(x: Float) = if (lensFacing == CameraSelector.LENS_FACING_FRONT) viewWidth - x * xFactor
                                        else x * xFactor
    private fun translateY(y: Float) = y * yFactor

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            viewWidth = width.toFloat()
            viewHeight = height.toFloat()
            frameWidth = frameSize.width.toFloat()
            frameHeight = frameSize.height.toFloat()

            // calculate factor between view size and frame size to scale
            xFactor = viewWidth / frameWidth
            yFactor = viewHeight / frameHeight


            faces.forEach { face ->
                faceBoxF = face.boundingBox.toRectF()

//                for (contours in face.allContours) {
//                    for (point in contours.points)
//                        canvas.drawPoint(translateX(contours.points[0].x), translateY(point.y), boxPaint)
//                    Log.d(TAG, "onDraw: " + contours)
//                }
//                               for (landmark in face.allLandmarks) {
//                    canvas.drawPoint(translateX(landmark.position.x), translateY(landmark.position.y), boxPaint)
//                }

                // face.allContours[0]: Face oval(36 points)
                // we select point index 25, 11, 21, 15: the four keypoints to do warping face
                val faceOval = face.allContours[0].points

                canvas.drawPoint(translateX(faceOval[25].x), translateY(faceOval[25].y), boxPaint)
                canvas.drawPoint(translateX(faceOval[11].x), translateY(faceOval[11].y), boxPaint)
                canvas.drawPoint(translateX(faceOval[21].x), translateY(faceOval[21].y), boxPaint)
                canvas.drawPoint(translateX(faceOval[15].x), translateY(faceOval[15].y), boxPaint)

                faceBoxF.set(
                    translateX(faceBoxF.left),
                    translateY(faceBoxF.top),
                    translateX(faceBoxF.right),
                    translateY(faceBoxF.bottom)
                )
                Log.d(TAG, "after: " + faceBoxF)
                canvas.drawRect(faceBoxF, boxPaint)
            }
        }
    }
    companion object {
        private val TAG = ResultView::class.java.simpleName
    }

}