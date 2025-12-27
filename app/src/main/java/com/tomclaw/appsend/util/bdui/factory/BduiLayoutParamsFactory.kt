package com.tomclaw.appsend.util.bdui.factory

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.bdui.model.BduiLayoutParams

/**
 * Factory for creating Android LayoutParams from BDUI layout parameters.
 */
class BduiLayoutParamsFactory(
    private val context: Context
) {

    fun create(params: BduiLayoutParams?, parent: ViewGroup): ViewGroup.LayoutParams {
        val width = parseDimension(params?.width, ViewGroup.LayoutParams.WRAP_CONTENT)
        val height = parseDimension(params?.height, ViewGroup.LayoutParams.WRAP_CONTENT)

        val layoutParams = when (parent) {
            is LinearLayout -> createLinearLayoutParams(width, height, params)
            is FrameLayout -> createFrameLayoutParams(width, height, params)
            is RecyclerView -> createRecyclerLayoutParams(width, height, params)
            else -> ViewGroup.MarginLayoutParams(width, height)
        }

        // Apply margins
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            params?.margin?.let { margin ->
                layoutParams.setMargins(
                    dpToPx(margin.getLeft()),
                    dpToPx(margin.getTop()),
                    dpToPx(margin.getRight()),
                    dpToPx(margin.getBottom())
                )
            }
        }

        return layoutParams
    }

    private fun createLinearLayoutParams(
        width: Int,
        height: Int,
        params: BduiLayoutParams?
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(width, height).apply {
            params?.weight?.let { weight = it }
            params?.layoutGravity?.let { gravity = parseGravity(it) }
        }
    }

    private fun createFrameLayoutParams(
        width: Int,
        height: Int,
        params: BduiLayoutParams?
    ): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(width, height).apply {
            params?.layoutGravity?.let { gravity = parseGravity(it) }
        }
    }

    private fun createRecyclerLayoutParams(
        width: Int,
        height: Int,
        params: BduiLayoutParams?
    ): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(width, height)
    }

    private fun parseDimension(value: String?, default: Int): Int {
        if (value == null) return default

        return when {
            value == "match_parent" || value == "fill_parent" -> ViewGroup.LayoutParams.MATCH_PARENT
            value == "wrap_content" -> ViewGroup.LayoutParams.WRAP_CONTENT
            value.endsWith("dp") -> dpToPx(value.removeSuffix("dp").toIntOrNull() ?: 0)
            value.endsWith("px") -> value.removeSuffix("px").toIntOrNull() ?: default
            else -> value.toIntOrNull()?.let { dpToPx(it) } ?: default
        }
    }

    private fun parseGravity(gravity: String): Int {
        var result = 0
        gravity.split("|").forEach { part ->
            result = result or when (part.trim().lowercase()) {
                "center" -> Gravity.CENTER
                "center_horizontal" -> Gravity.CENTER_HORIZONTAL
                "center_vertical" -> Gravity.CENTER_VERTICAL
                "start", "left" -> Gravity.START
                "end", "right" -> Gravity.END
                "top" -> Gravity.TOP
                "bottom" -> Gravity.BOTTOM
                "fill" -> Gravity.FILL
                "fill_horizontal" -> Gravity.FILL_HORIZONTAL
                "fill_vertical" -> Gravity.FILL_VERTICAL
                else -> 0
            }
        }
        return if (result == 0) Gravity.NO_GRAVITY else result
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}

