package com.tomclaw.appsend.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.tomclaw.appsend.R
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * A subclass of [android.view.View] class for creating a custom circular progressBar
 *
 *
 * Created by Pedram on 2015-01-06.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class CircleProgressView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    /**
     * ProgressBar's line thickness
     */
    var strokeWidth = 4f
        set(value) {
            field = value
            backgroundPaint.strokeWidth = value
            foregroundPaint.strokeWidth = value
            invalidate()
            requestLayout() //Because it should recalculate its bounds
        }
    @SuppressLint("AnimatorKeep")
    var progress = 0f
        set(value) {
            field = value
            invalidate()
        }
    var min = 0
        set(value) {
            field = value
            invalidate()
        }
    var max = 100
        set(value) {
            field = value
            invalidate()
        }

    /**
     * Start the progress at 12 o'clock
     */
    private val startAngle = -90
    var color = Color.DKGRAY
        set(value) {
            field = value
            backgroundPaint.color = adjustAlpha(value, 0.3f)
            foregroundPaint.color = value
            invalidate()
            requestLayout()
        }
    private val rectF: RectF = RectF()
    private val backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val foregroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressView,
                0, 0)
        //Reading values from the XML layout
        try {
            strokeWidth = typedArray.getDimension(R.styleable.CircleProgressView_progressBarThickness, strokeWidth)
            progress = typedArray.getFloat(R.styleable.CircleProgressView_progress, progress)
            color = typedArray.getInt(R.styleable.CircleProgressView_progressbarColor, color)
            min = typedArray.getInt(R.styleable.CircleProgressView_min, min)
            max = typedArray.getInt(R.styleable.CircleProgressView_max, max)
        } finally {
            typedArray.recycle()
        }

        backgroundPaint.color = adjustAlpha(color, 0.3f)
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = strokeWidth

        foregroundPaint.color = color
        foregroundPaint.style = Paint.Style.STROKE
        foregroundPaint.strokeWidth = strokeWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawOval(rectF, backgroundPaint)
        val angle = 360 * progress / max
        canvas.drawArc(rectF, startAngle.toFloat(), angle, false, foregroundPaint)

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = min(width, height)
        setMeasuredDimension(min, min)
        rectF.set(
                0 + strokeWidth / 2,
                0 + strokeWidth / 2,
                min - strokeWidth / 2,
                min - strokeWidth / 2
        )
    }

    /**
     * Lighten the given color by the factor
     *
     * @param color  The color to lighten
     * @param factor 0 to 4
     * @return A brighter color
     */
    fun lightenColor(color: Int, factor: Float): Int {
        val r = Color.red(color) * factor
        val g = Color.green(color) * factor
        val b = Color.blue(color) * factor
        val ir = min(255, r.toInt())
        val ig = min(255, g.toInt())
        val ib = min(255, b.toInt())
        val ia = Color.alpha(color)
        return Color.argb(ia, ir, ig, ib)
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    /**
     * Set the progress with an animation.
     * Note that the [android.animation.ObjectAnimator] Class automatically set the progress
     * so don't call the [CircleProgressView.progress] directly within this method.
     *
     * @param progress The progress it should animate to it.
     * @param duration Progress animation time.
     */
    @SuppressLint("AnimatorKeep")
    fun setProgressWithAnimation(progress: Float, duration: Long = 1500) {
        val objectAnimator = ObjectAnimator.ofFloat(this, "progress", progress)
        objectAnimator.duration = duration
        objectAnimator.interpolator = LinearInterpolator()
        objectAnimator.start()
    }

}
