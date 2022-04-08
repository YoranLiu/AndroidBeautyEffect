package com.jack.beautyeffect.beautyUtils

import android.content.Context
import android.graphics.Bitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.*
import org.opencv.android.Utils

class ResurfacingUtils {
    // gpuImage method
    fun resurface(bitmap: Bitmap, context: Context, satuation: Int): Bitmap {


        val gpuImage = GPUImage(context).apply {
            setImage(bitmap)
            setFilter(GPUImageBilateralBlurFilter(30f - satuation/4f))
        }
        return gpuImage.bitmapWithFilterApplied
    }
    // opencv method
    fun resurface(bitmap: Bitmap): Bitmap {
        return bitmap
    }
}