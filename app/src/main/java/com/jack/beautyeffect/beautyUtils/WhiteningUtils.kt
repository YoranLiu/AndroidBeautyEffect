package com.jack.beautyeffect.beautyUtils

import android.content.Context
import android.graphics.Bitmap
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBilateralBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter

class WhiteningUtils {

    // gpuImage method
    fun whitening(bitmap: Bitmap, context: Context, satuation: Int): Bitmap {

        val gpuImage = GPUImage(context).apply {
            setImage(bitmap)
            setFilter(GPUImageBrightnessFilter(satuation/1000f))
        }
        return gpuImage.bitmapWithFilterApplied
    }
}