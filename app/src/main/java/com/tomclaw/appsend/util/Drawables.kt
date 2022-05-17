package com.tomclaw.appsend.util

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.caverock.androidsvg.SVG

fun svgToDrawable(icon: String, resources: Resources): Drawable {
    val picture = SVG.getFromString(icon).renderToPicture()
    val bitmap = picture.toBitmap(
        bitmapWidth = dpToPx(picture.width, resources),
        bitmapHeight = dpToPx(picture.height, resources)
    )
    return BitmapDrawable(resources, bitmap)
}