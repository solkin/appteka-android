package com.tomclaw.appsend.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedInputStream
import java.io.InputStream
import kotlin.math.sqrt

fun decodeSampledBitmapFromStream(stream: InputStream, maxPixels: Int): Bitmap? {
    var bitmap: Bitmap?
    try {
        val inputStream: InputStream = BufferedInputStream(
            stream,
            THUMBNAIL_BUFFER_SIZE
        )
        inputStream.mark(THUMBNAIL_BUFFER_SIZE)

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)

        // Calculate inSampleSize
        val reqSize = calculateImageSize(width = options.outWidth, height = options.outHeight, maxPixels)
        val widthSample = (options.outWidth / reqSize.first).toFloat()
        val heightSample = (options.outHeight / reqSize.second).toFloat()
        var scaleFactor = widthSample.coerceAtLeast(heightSample)
        if (scaleFactor < 1) {
            scaleFactor = 1f
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor.toInt()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        // Decode bitmap with inSampleSize set
        inputStream.reset()
        bitmap = BitmapFactory.decodeStream(inputStream, null, options)
    } catch (ignored: Throwable) {
        bitmap = null
    }
    return bitmap
}

fun calculateImageSize(width: Int, height: Int, maxPixels: Int): Pair<Int, Int> {
    val pixels = width * height
    if (pixels > maxPixels) {
        val resizeWidth = sqrt(maxPixels.toFloat() * width.toFloat() / height.toFloat())
        val resizeHeight = resizeWidth * height.toFloat() / width.toFloat()
        return resizeWidth.toInt() to resizeHeight.toInt()
    }
    return width to height
}

const val THUMBNAIL_BUFFER_SIZE = 128 * 1024
