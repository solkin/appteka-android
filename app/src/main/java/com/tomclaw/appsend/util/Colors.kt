package com.tomclaw.appsend.util

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import com.tomclaw.appsend.R
import kotlin.math.max

/**
 * Returns darker version of specified `color`.
 */
fun darker(color: Int, factor: Float): Int {
    val a = Color.alpha(color)
    val r = Color.red(color)
    val g = Color.green(color)
    val b = Color.blue(color)
    return Color.argb(
        a,
        max((r * factor).toInt(), 0),
        max((g * factor).toInt(), 0),
        max((b * factor).toInt(), 0)
    )
}

fun getAttributedColor(context: Context, attr: Int): Int {
    val set = intArrayOf(attr)
    val typedValue = TypedValue()
    val a = context.obtainStyledAttributes(typedValue.data, set)
    val color = a.getColor(0, Color.WHITE)
    a.recycle()
    return color
}

fun getColor(color: Int, context: Context): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.resources.getColor(R.color.primary_color, context.theme)
    } else {
        context.resources.getColor(R.color.primary_color)
    }
}