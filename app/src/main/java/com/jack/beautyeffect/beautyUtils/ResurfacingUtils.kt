package com.jack.beautyeffect.beautyUtils

import android.content.Context
import android.graphics.Bitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

class ResurfacingUtils {
    // gpuImage method
    fun resurface(bitmap: Bitmap, context: Context, strength: Int): Bitmap {

        val gpuImage = GPUImage(context).apply {
            setImage(bitmap)
            setFilter(GPUImageBilateralBlurFilter(30f - strength/4f))
        }
        return gpuImage.bitmapWithFilterApplied
    }

    // opencv method
    fun resurface(bitmap: Bitmap, strength: Int): Bitmap {
        val img = Mat()
        val bilImg = Mat()
        val subImg = Mat()
        val gausImgSrc = Mat()
        val gausImgDst = Mat()
        val tmpImgSrc = Mat()
        val tmpImgDst = Mat()
        val imgRes = Mat()

        val value = strength / 100.0 * 2.5
        val d =  value * 5
        val fc = value * 12.5
        val p = 0.1

        Utils.bitmapToMat(bitmap, img)
        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGRA2BGR)
        Imgproc.bilateralFilter(img, bilImg, d.toInt(), fc, fc)

        Core.subtract(bilImg, img, subImg)
        Core.add(subImg, Scalar(128.0,128.0,128.0,128.0), gausImgSrc)


        Imgproc.GaussianBlur(gausImgSrc, gausImgDst, Size(5.0, 5.0), 0.0, 0.0)
        gausImgDst.convertTo(tmpImgSrc, gausImgDst.type(), 2.0, -255.0)
        Core.add(img, tmpImgSrc, tmpImgDst)
        Core.addWeighted(img, p, tmpImgDst, 1.0 - p, 0.0, imgRes)

        Core.add(imgRes, Scalar(10.0, 10.0, 10.0), imgRes)

        Utils.matToBitmap(imgRes, bitmap)
        return bitmap
    }
}