package com.jack.beautyeffect.beautyUtils

import android.graphics.*

class SmallFaceUtils {

    val GRID_W = 200
    val GRID_H = 200

    /**
     *  small Face algorithm
     *  @param bitmap: original bitmap
     *  @param facePoints: Face oval(36 points) from face.allContours[0]
     *  @param centerPoints: Nose bridge(2 points) from face.allContours[11]
     *  @param level: level of stretch
     *  @return result bitmap after doing warping face process
     */
    fun smallFace(
        bitmap: Bitmap,
        facePoints: List<PointF>,
        centerPoints: List<PointF>,
        level: Int = 5
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
                fx = (bitmapW * j / GRID_W).toFloat()
                //X轴坐标 放在偶数位
                verts[idx * 2] = fx
                //Y轴坐标 放在奇数位
                verts[idx * 2 + 1] = fy
                idx += 1
            }
        }
        val r = 180 + 15 * level //level [0,4]



        // left face points
        warpFace(verts, facePoints[25].x, facePoints[25].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[24].x, facePoints[24].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[22].x, facePoints[22].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[20].x, facePoints[20].y, centerPoints[1].x, centerPoints[1].y, r)

        // right face points
        warpFace(verts, facePoints[11].x, facePoints[11].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[12].x, facePoints[12].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[14].x, facePoints[14].y, centerPoints[1].x, centerPoints[1].y, r)
        warpFace(verts, facePoints[16].x, facePoints[16].y, centerPoints[1].x, centerPoints[1].y, r)


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
        //计算拖动距离
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
                //边界区域不处理
                if (i < offset || i > GRID_H - offset || j < offset || j > GRID_W - offset) {
                    idx += 1
                    continue
                }
                //计算每个坐标点与触摸点之间的距离
                val dx = verts[idx * 2] - startX
                val dy = verts[idx * 2 + 1] - startY
                val dd = dx * dx + dy * dy
                if (dd < powR) {
                    //变形系数，扭曲度
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
//     fun warpFace (
//        startX: Float,
//        startY: Float,
//        endX: Float,
//        endY: Float,
//     ): FloatArray {
//        //level [0,4]
//
//        val r = 180 + 15 * level
//
//        val ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY)
//        var dPull = Math.sqrt(ddPull.toDouble()).toFloat()
//        //dPull = screenWidth - dPull >= 0.0001f ? screenWidth - dPull : 0.0001f;
//        if (dPull < 2 * r) {
//            dPull = (2 * r).toFloat()
//        }
//        val powR = r * r
//        var idx = 0
//        val offset = 1
//        for (i in 0 until gridH + 1) {
//            for (j in 0 until gridW + 1) {
//                //边界区域不处理
//                if (i < offset || i > gridH - offset || j < offset || j > gridW - offset) {
//                    idx += 1
//                    continue
//                }
//                //计算每个坐标点与触摸点之间的距离
//                val dx = verts[idx * 2] - startX
//                val dy = verts[idx * 2 + 1] - startY
//                val dd = dx * dx + dy * dy
//                if (dd < powR) {
//                    //变形系数，扭曲度
//                    val e =
//                        ((powR - dd) * (powR - dd) / ((powR - dd + dPull * dPull) * (powR - dd + dPull * dPull))).toDouble()
//                    val pullX = e * (endX - startX)
//                    val pullY = e * (endY - startY)
//                    verts[idx * 2] = (verts[idx * 2] + pullX).toFloat()
//                    verts[idx * 2 + 1] = (verts[idx * 2 + 1] + pullY).toFloat()
//                }
//                idx += 1
//            }
//        }
//
//         return verts
//    }
//}