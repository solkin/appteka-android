package com.tomclaw.appsend.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.drawable.Drawable

enum class Corner {
    LEFT, RIGHT, NONE
}

class BubbleColorDrawable(
    context: Context,
    private val color: Int,
    private val corner: Corner
) : Drawable() {

    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var offset = dp(8f, context).toFloat()
    private var bubbleRadius = dp(6f, context).toFloat()

    override fun setColorFilter(cf: ColorFilter?) {}

    override fun setAlpha(alpha: Int) {}

    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun draw(canvas: Canvas) {
        val r = RectF(bounds)
        val rect = RectF(r)
        rect.inset(offset, offset)
        val path = Path()
        when (corner) {
            Corner.LEFT -> {
                path.moveTo(0f, 0f)
                path.lineTo(r.width() - offset, 0f)
                path.arcTo(
                    RectF(
                        r.right - bubbleRadius * 2f,
                        0f,
                        r.right,
                        bubbleRadius * 2f
                    ), 270f, 90f
                )
                path.lineTo(r.width(), r.height() - offset)
                path.arcTo(
                    RectF(
                        r.right - bubbleRadius * 2f,
                        r.bottom - bubbleRadius * 2f,
                        r.right,
                        r.bottom
                    ),
                    0f,
                    90f
                )
                path.lineTo(bubbleRadius, r.height())
                path.arcTo(
                    RectF(
                        offset,
                        r.bottom - bubbleRadius * 2f,
                        bubbleRadius * 2f + offset,
                        r.bottom
                    ),
                    90f,
                    90f
                )
                path.lineTo(offset, offset)
            }

            Corner.RIGHT -> {
                path.moveTo(bubbleRadius, 0f)
                path.lineTo(r.width(), 0f)
                path.lineTo(r.width() - offset, offset)
                path.lineTo(r.width() - offset, (r.height() - bubbleRadius))
                path.arcTo(
                    RectF(
                        r.right - bubbleRadius * 2f - offset,
                        r.bottom - bubbleRadius * 2f,
                        r.right - offset,
                        r.bottom
                    ),
                    0f,
                    90f
                )
                path.lineTo(bubbleRadius, r.height())
                path.arcTo(
                    RectF(
                        0f,
                        r.bottom - bubbleRadius * 2f,
                        bubbleRadius * 2f,
                        r.bottom
                    ), 90f, 90f
                )
                path.lineTo(0f, bubbleRadius)
                path.arcTo(
                    RectF(
                        0f,
                        0f,
                        bubbleRadius * 2f,
                        bubbleRadius * 2f
                    ),
                    180f,
                    90f
                )
            }

            Corner.NONE -> {
                rect.inset(-offset, -offset)
                path.addRoundRect(
                    rect,
                    bubbleRadius,
                    bubbleRadius,
                    Path.Direction.CW
                )
            }
        }
        path.close()
        whitePaint.color = color
        whitePaint.style = Paint.Style.FILL
        canvas.drawPath(path, whitePaint)
    }

    private fun dp(v: Float, context: Context): Int {
        return (v * context.resources.displayMetrics.density + 0.5).toInt()
    }
}
