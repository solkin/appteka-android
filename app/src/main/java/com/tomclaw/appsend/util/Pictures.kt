package com.tomclaw.appsend.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Picture
import android.graphics.Rect

fun Picture.toBitmap(
    config: Bitmap.Config = Bitmap.Config.ARGB_8888,
    bitmapWidth: Int = width,
    bitmapHeight: Int = height
): Bitmap {
    val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, config)
    val canvas = Canvas(bitmap)
    val rect = Rect(0, 0, bitmapWidth, bitmapHeight)
    canvas.drawPicture(this, rect)
    canvas.setBitmap(null)
    return bitmap
}
