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
import com.jack.beautyeffect.BitmapUtils.rotateBitmap
import com.jack.beautyeffect.beautyUtils.SmallFaceUtils

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

    private var bitmap: Bitmap? = null
    private var transformMatrix = Matrix()

    val GRID_W = 200
    val GRID_H = 200
    val COUNT = (GRID_W + 1) * (GRID_H + 1)
    var verts = FloatArray(COUNT * 2)

    var frameSize = Size(0, 0)

    init {
        boxPaint.color = Color.RED
        boxPaint.style = Paint.Style.STROKE
        boxPaint.strokeWidth = 5.0f
        setWillNotDraw(false)
    }

    fun updateFaces(faces: List<Face>, lensFacing: Int, bitmap: Bitmap) {
        this.faces = faces
        this.lensFacing = lensFacing
        this.bitmap = bitmap
        restoreVerts()
        invalidate()
    }

    private fun restoreVerts() {
        var idx = 0
        val bmWidth = bitmap!!.width.toFloat()
        val bmHeight = bitmap!!.height.toFloat()
        for (i in 0 until GRID_H + 1) {
            val fy: Float = bmHeight * i / GRID_H
            for (j in 0 until GRID_W + 1) {
                val fx: Float = bmWidth * j / GRID_W
                //X轴坐标 放在偶数位
                verts[idx * 2] = fx
                //Y轴坐标 放在奇数位
                verts[idx * 2 + 1] = fy
                idx += 1
            }
        }
    }
    // In front camera situation, the detection result coordinates will be mirror, we need to reverse these coordinates
    private fun translateX(x: Float) = if (lensFacing == CameraSelector.LENS_FACING_FRONT) bitmap!!.width - x
                                        else x
    private fun translateY(y: Float) = y * yFactor

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            // canvas size equals to view size

            viewWidth = width.toFloat()
            viewHeight = height.toFloat()
            frameWidth = frameSize.width.toFloat()
            frameHeight = frameSize.height.toFloat()

            // calculate factor between view size and frame size to scale
            xFactor = viewWidth / frameWidth
            yFactor = viewHeight / frameHeight

            faces.forEach { face ->
                faceBoxF = face.boundingBox.toRectF()


//                if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
//                    transformMatrix.postScale(-1.0f, 1.0f)
//                    bitmap = Bitmap.createBitmap(bitmap!!, 0, 0, bitmap!!.width, bitmap!!.height, transformMatrix, false)
//                }

                 canvas.drawBitmap(bitmap!!, 500f,500f, boxPaint)

                // need to check if countours have been detected
                if (face.allContours.size > 11) {
//                    canvas.drawPoint(translateX(faceOval[25].x), translateY(faceOval[25].y), boxPaint)
//                    canvas.drawPoint(translateX(faceOval[11].x), translateY(faceOval[11].y), boxPaint)
//                    canvas.drawPoint(translateX(faceOval[21].x), translateY(faceOval[21].y), boxPaint)
//                    canvas.drawPoint(translateX(faceOval[15].x), translateY(faceOval[15].y), boxPaint)

//                    canvas.drawPoint(translateX(faceOval[25].x), faceOval[25].y, boxPaint)
//                    canvas.drawPoint(translateX(faceOval[11].x), faceOval[11].y, boxPaint)
//                    canvas.drawPoint(translateX(faceOval[21].x), faceOval[21].y, boxPaint)
//                    canvas.drawPoint(translateX(faceOval[15].x), faceOval[15].y, boxPaint)


                    // we choose 2 left face points, 2 right face points, and 1 center point to do warping face
                    // we select point index 25, 21(left face points), 11, 15(right face points): the four keypoints
                    // face.allContours[0]: Face oval(36 points), face.allContours[11]: Nose bridge(2 points)
                    val faceOval = face.allContours[0].points
                    val noseBridge = face.allContours[11].points
                    canvas.drawPoint(faceOval[25].x, faceOval[25].y, boxPaint)
                    canvas.drawPoint(faceOval[21].x, faceOval[21].y, boxPaint)
                    canvas.drawPoint(faceOval[11].x, faceOval[11].y, boxPaint)
                    canvas.drawPoint(faceOval[15].x, faceOval[15].y, boxPaint)

                    // we choose nose bridge point index 1 as center point
                    canvas.drawPoint(noseBridge[1].x, noseBridge[1].y, boxPaint)

                    val a = SmallFaceUtils(GRID_W, GRID_H, COUNT, verts, level=10)

                    verts = a.warpFace(faceOval[25].x, faceOval[25].y, noseBridge[1].x, noseBridge[1].y)

                    verts = a.warpFace(faceOval[21].x, faceOval[21].y, noseBridge[1].x, noseBridge[1].y)

                    verts = a.warpFace(faceOval[11].x, faceOval[11].y, noseBridge[1].x, noseBridge[1].y)

                    verts = a.warpFace(faceOval[15].x, faceOval[15].y, noseBridge[1].x, noseBridge[1].y)

                    canvas.drawBitmapMesh(bitmap!!, GRID_W, GRID_H, verts, 0, null, 0, boxPaint)
                }

//                faceBoxF.set(
//                    translateX(faceBoxF.left),
//                    faceBoxF.top,
//                    translateX(faceBoxF.right),
//                    faceBoxF.bottom
//                )

                //canvas.drawRect(faceBoxF, boxPaint)

            }
        }
    }
    companion object {
        private val TAG = ResultView::class.java.simpleName
    }

}