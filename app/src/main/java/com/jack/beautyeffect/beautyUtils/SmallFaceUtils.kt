package com.jack.beautyeffect.beautyUtils

import android.graphics.*

class SmallFaceUtils(var gridW: Int, var gridH: Int, var count: Int, var verts: FloatArray, var level: Int = 5) {

     fun warpFace (
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
     ): FloatArray {
        //level [0,4]

        val r = 180 + 15 * level

        val ddPull = (endX - startX) * (endX - startX) + (endY - startY) * (endY - startY)
        var dPull = Math.sqrt(ddPull.toDouble()).toFloat()
        //dPull = screenWidth - dPull >= 0.0001f ? screenWidth - dPull : 0.0001f;
        if (dPull < 2 * r) {
            dPull = (2 * r).toFloat()
        }
        val powR = r * r
        var idx = 0
        val offset = 1
        for (i in 0 until gridH + 1) {
            for (j in 0 until gridW + 1) {
                //边界区域不处理
                if (i < offset || i > gridH - offset || j < offset || j > gridW - offset) {
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

         return verts
    }
}