package com.avito.android.krop

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef

abstract class OverlayView(context: Context, attrs: AttributeSet? = null) :
        View(context, attrs), ViewportUpdateListener {

    private var overlayColor: Int = Color.TRANSPARENT
    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private lateinit var viewport: RectF
    private lateinit var measureListener: MeasureListener

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        check(::measureListener.isInitialized) {
            "Overlay not inited correctly: check, if it is referenced by any MeasureListener implementation"
        }
        measureListener.onOverlayMeasured()
    }

    override fun onUpdateViewport(newViewport: RectF) {
        viewport = newViewport
    }

    fun setOverlayColor(color: Int) {
        overlayColor = color
        invalidate()
    }

    fun setMeasureListener(listener: MeasureListener) {
        measureListener = listener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(overlayColor)
        canvas.drawViewportView(viewport, clearPaint)
    }

    /**
     * @param viewport focus window rectangle on canvas
     * @param clearPaint paint for removing color in custom area of canvas
     */
    protected abstract fun Canvas.drawViewportView(viewport: RectF, clearPaint: Paint)

    interface MeasureListener {
        fun onOverlayMeasured()
    }
}

class OvalOverlay(context: Context, attrs: AttributeSet? = null) : OverlayView(context, attrs) {

    override fun Canvas.drawViewportView(viewport: RectF, clearPaint: Paint) {
        drawOval(viewport, clearPaint)
    }
}

class RectOverlay(context: Context, attrs: AttributeSet? = null) : OverlayView(context, attrs) {

    override fun Canvas.drawViewportView(viewport: RectF, clearPaint: Paint) {
        drawRect(viewport, clearPaint)
    }
}

@IntDef(SHAPE_OVAL, SHAPE_RECT)
@Retention(AnnotationRetention.SOURCE)
annotation class OverlayShape

const val SHAPE_CUSTOM = -1
const val SHAPE_OVAL = 0
const val SHAPE_RECT = 1