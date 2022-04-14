package com.jack.beautyeffect.beautyUtils

import android.graphics.*

class SmallFaceUtils {

    val GRID_W = 20
    val GRID_H = 20

    /**
     *  small Face algorithm
     *  @param bitmap: original bitmap
     *  @param facePoints: Face oval(36 points) from face.allContours[0]
     *  @param centerPoints: Nose bridge(2 points) from face.allContours[11]
     *  @param strength: small face strength
     *  @return result bitmap after doing warping face process
     */
    fun smallFace(
        bitmap: Bitmap,
        facePoints: List<PointF>,
        centerPoints: List<PointF>,
        strength: Int
    ): Bitmap {
        val COUNT = (GRID_W + 1) * (GRID_H + 1)
        val verts = FloatArray(COUNT * 2)
        val bitmapW = bitmap.width
        val bitmapH = bitmap.height

        var idx = 0
        var fx = 0f
        var fy = 0f

        for (i in 0 until GRID_H + 1) {
            fy = (bitmapH * i / GRID_H).toFloat()
            for (j in 0 until GRID_W + 1) {
                // X-axis coordinates, put in even position
                fx = (bitmapW * j / GRID_W).toFloat()
                verts[idx * 2] = fx

                // Y-axis coordinates, put in odd position
                verts[idx * 2 + 1] = fy
                idx += 1
            }
        }
        val r = 0 + (1.25 * strength).toInt()


        /**
         * Choose your own key points to do warping
         */
        // left face points
        warpFace(verts, facePoints[25].x, facePoints[25].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[23].x, facePoints[23].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[21].x, facePoints[21].y, centerPoints[1].x, centerPoints[1].y, r)
        //warpFace(verts, facePoints[24].x, facePoints[24].y, centerPoints[1].x, centerPoints[1].y, r)
        //warpFace(verts, facePoints[22].x, facePoints[22].y, centerPoints[1].x, centerPoints[1].y, r)
        //warpFace(verts, facePoints[20].x, facePoints[20].y, centerPoints[1].x, centerPoints[1].y, r)

        // right face points
        warpFace(verts, facePoints[11].x, facePoints[11].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[13].x, facePoints[13].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[15].x, facePoints[15].y, centerPoints[1].x, centerPoints[1].y, r)


//        warpFace(verts, facePoints[12].x, facePoints[12].y, centerPoints[1].x, centerPoints[1].y, r)
//        warpFace(verts, facePoints[14].x, facePoints[14].y, centerPoints[1].x, centerPoints[1].y, r)
//        warpFace(verts, facePoints[16].x, facePoints[16].y, centerPoints[1].x, centerPoints[1].y, r)


        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        canvas.drawBitmapMesh(bitmap, GRID_W, GRID_H, verts, 0, null, 0, null)
        return resultBitmap
    }

    private fun warpFace(
        verts: FloatArray,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        r: Int
    ) {
        // calculate the drag distance
        val ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY)
        var dPull = Math.sqrt(ddPull.toDouble()).toFloat()

        if (dPull < 2 * r) {
            dPull = (2 * r).toFloat()
        }

        val powR = r * r
        var idx = 0
        val offset = 1
        for (i in 0 until GRID_H + 1) {
            for (j in 0 until GRID_W + 1) {
                // skip bounding area
                if (i < offset || i > GRID_H - offset || j < offset || j > GRID_W - offset) {
                    idx += 1
                    continue
                }
                // calculate the distance between each point and the touch point
                val dx = verts[idx * 2] - startX
                val dy = verts[idx * 2 + 1] - startY
                val dd = dx * dx + dy * dy
                if (dd < powR) {
                    // torsion resistance
                    val e =
                        ((powR - dd) * (powR - dd) / ((powR - dd + dPull * dPull) * (powR - dd + dPull * dPull))).toDouble()
                    val pullX = e * (endX - startX)
                    val pullY = e * (endY - startY)
                    verts[idx * 2] = (verts[idx * 2] + pullX).toFloat()
                    verts[idx * 2 + 1] = (verts[idx * 2 + 1] + pullY).toFloat()
                }
                idx += 1
            }
        }
    }
}
