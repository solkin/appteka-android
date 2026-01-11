package com.tomclaw.appsend.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.createBitmap
import com.tomclaw.appsend.R
import java.io.ByteArrayOutputStream

object PackageHelper {

    fun getPackageIconPng(
        info: ApplicationInfo?,
        packageManager: PackageManager,
        context: Context
    ): ByteArray {
        val icon = info?.loadIcon(packageManager)
            ?: AppCompatResources.getDrawable(context, R.drawable.app_placeholder)
            ?: return ByteArray(0)
        val bitmap = drawableToBitmap(icon)
        return ByteArrayOutputStream().use { baos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            baos.toByteArray()
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }

        val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            createBitmap(width = 1, height = 1)
        } else {
            createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

}
