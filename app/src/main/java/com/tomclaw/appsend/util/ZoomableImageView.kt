package com.tomclaw.appsend.util

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import android.widget.Scroller
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/*
 * ZoomableImageView allows you to zoom and move bitmap on view with touch effects
 * Based on TouchImageView by Michael Ortiz
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ZoomableImageView : AppCompatImageView {

    private enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    private lateinit var imgMatrix: Matrix
    private lateinit var prevMatrix: Matrix

    private var state: State? = null

    private var minScale: Float = 0.0f
    private var maxScale: Float = 0.0f
    private var superMinScale: Float = 0.0f
    private var superMaxScale: Float = 0.0f
    private lateinit var matrix: FloatArray

    private var fling: Fling? = null

    private val lastMovePoint = PointF()

    private lateinit var imageScaleType: ScaleType

    private var imageRenderedAtLeastOnce: Boolean = false
    private var onDrawReady: Boolean = false

    private var delayedZoomVariables: ZoomVariables? = null

    private var realSize = SizeF()
    private var viewSize = SizeF()
    private var prevViewSize = SizeF()

    private var matchViewSize = SizeF()
    private var prevMatchViewSize = SizeF()

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector

    var doubleTapListener: GestureDetector.OnDoubleTapListener? = null
    var userTouchListener: OnTouchListener? = null
    var imageMoveListener: ImageMoveListener? = null

    private val imageWidth: Float
        get() = matchViewSize.width * currentZoom

    private val imageHeight: Float
        get() = matchViewSize.height * currentZoom

    var viewport = RectF()

    val scrollPosition: PointF?
        get() {
            val drawable = drawable ?: return null
            val drawableWidth = drawable.intrinsicWidth
            val drawableHeight = drawable.intrinsicHeight

            val point = transformCoordTouchToBitmap(x = viewSize.width / 2, y = viewSize.height / 2, clipToBitmap = true)
            point.x /= drawableWidth.toFloat()
            point.y /= drawableHeight.toFloat()
            return point
        }

    var currentZoom: Float = 0.0f
        private set

    val isZoomed: Boolean
        get() = currentZoom != 1.0f

    val zoomedRect: RectF
        get() {
            if (imageScaleType == ScaleType.FIT_XY) {
                throw UnsupportedOperationException("getZoomedRect() not supported with FIT_XY")
            }
            val topLeft = transformCoordTouchToBitmap(x = 0.0f, y = 0.0f, clipToBitmap = true)
            val bottomRight = transformCoordTouchToBitmap(x = viewSize.width, y = viewSize.height, clipToBitmap = true)

            val w = drawable.intrinsicWidth.toFloat()
            val h = drawable.intrinsicHeight.toFloat()
            return RectF(topLeft.x / w, topLeft.y / h, bottomRight.x / w, bottomRight.y / h)
        }

    var maxZoom: Float
        get() = maxScale
        set(max) {
            maxScale = max
            superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        }

    var minZoom: Float
        get() = minScale
        set(min) {
            minScale = min
            superMinScale = SUPER_MIN_MULTIPLIER * minScale
        }

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        sharedConstructing(context)
    }

    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
        imgMatrix = Matrix()
        prevMatrix = Matrix()
        matrix = FloatArray(9)
        currentZoom = 1.0f
        imageScaleType = ScaleType.CENTER_CROP
        minScale = 1.0f
        maxScale = 5.0f
        superMinScale = SUPER_MIN_MULTIPLIER * minScale
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        imageMatrix = imgMatrix
        scaleType = ScaleType.MATRIX
        state = State.NONE
        onDrawReady = false
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        savePreviousImageValues()
        fitImageToView()
    }

    override fun setScaleType(type: ScaleType) {
        if (type == ScaleType.FIT_START || type == ScaleType.FIT_END) {
            throw UnsupportedOperationException("ZoomableImageView does not support FIT_START or FIT_END")
        }
        if (type == ScaleType.MATRIX) {
            super.setScaleType(ScaleType.MATRIX)
        } else {
            imageScaleType = type
            if (onDrawReady) {
                setZoom(this)
            }
        }
    }

    override fun getScaleType(): ScaleType {
        return imageScaleType
    }

    private fun savePreviousImageValues() {
        if (viewSize.height != 0.0f && viewSize.width != 0.0f) {
            imgMatrix.getValues(matrix)
            prevMatrix.setValues(matrix)
            prevMatchViewSize.height = matchViewSize.height
            prevMatchViewSize.width = matchViewSize.width
            prevViewSize.height = viewSize.height
            prevViewSize.width = viewSize.width
        }
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        imgMatrix.getValues(matrix)
        return SavedState(
            superState!!,
            currentZoom,
            matrix,
            matchViewSize,
            viewSize,
            imageRenderedAtLeastOnce
        )
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            currentZoom = state.currentZoom
            matrix = state.matrix
            prevMatchViewSize = state.prevMatchViewSize
            prevViewSize = state.prevViewSize
            imageRenderedAtLeastOnce = state.imageRenderedAtLeastOnce

            prevMatrix.setValues(matrix)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onDraw(canvas: Canvas) {
        onDrawReady = true
        imageRenderedAtLeastOnce = true
        delayedZoomVariables?.let {
            setZoom(it.scale, it.focusX, it.focusY, it.scaleType)
            delayedZoomVariables = null
        }
        super.onDraw(canvas)
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        savePreviousImageValues()
    }

    fun resetZoom() {
        currentZoom = 1f
        fitImageToView()
    }

    @JvmOverloads
    fun setZoom(
        scale: Float,
        focusX: Float = 0.5f,
        focusY: Float = 0.5f,
        scaleType: ScaleType = imageScaleType
    ) {
        if (!onDrawReady) {
            delayedZoomVariables = ZoomVariables(scale, focusX, focusY, scaleType)
            return
        }
        if (scaleType != imageScaleType) {
            setScaleType(scaleType)
        }
        resetZoom()
        scaleImage(deltaScale = scale.toDouble(), focusX = viewSize.width / 2, focusY = viewSize.height / 2, stretchImageToSuper = true)
        imgMatrix.getValues(matrix)
        matrix[Matrix.MTRANS_X] = -(focusX * imageWidth - viewSize.width / 2)
        matrix[Matrix.MTRANS_Y] = -(focusY * imageHeight - viewSize.height / 2)
        imgMatrix.setValues(matrix)
        fixTrans()
        imageMatrix = imgMatrix
    }

    fun setZoom(img: ZoomableImageView) {
        img.scrollPosition?.let { center ->
            setZoom(img.currentZoom, center.x, center.y, img.scaleType)
        }
    }

    fun setScrollPosition(focusX: Float, focusY: Float) {
        setZoom(currentZoom, focusX, focusY)
    }

    private fun fixTrans() {
        imgMatrix.getValues(matrix)
        val transX = matrix[Matrix.MTRANS_X]
        val transY = matrix[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewSize.width, imageWidth)
        val fixTransY = getFixTrans(transY, viewSize.height, imageHeight)

        if (fixTransX != 0.0f || fixTransY != 0.0f) {
            imgMatrix.postTranslate(fixTransX, fixTransY)
        }
    }

    private fun fixScaleTrans() {
        fixTrans()
        imgMatrix.getValues(matrix)
        if (imageWidth < viewSize.width) {
            matrix[Matrix.MTRANS_X] = (viewSize.width - imageWidth) / 2
        }
        if (imageHeight < viewSize.height) {
            matrix[Matrix.MTRANS_Y] = (viewSize.height - imageHeight) / 2
        }
        imgMatrix.setValues(matrix)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0.0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0.0f
        }

        if (trans < minTrans) return -trans + minTrans
        if (trans > maxTrans) return -trans + maxTrans

        return 0.0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        if (contentSize <= viewSize) {
            return 0.0f
        }
        return delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            setMeasuredDimension(0, 0)
            return
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        realSize.width = setViewSize(widthMode, widthSize, drawableWidth).toFloat()
        realSize.height = setViewSize(heightMode, heightSize, drawableHeight).toFloat()

        if (viewport.isEmpty) {
            with(viewport) {
                left = 0.0f
                top = 0.0f
                right = realSize.width
                bottom = realSize.height
            }
        }

        viewSize.width = viewport.width()
        viewSize.height = viewport.height()

        val rect = realSize.middle(viewSize)
        setMeasuredDimension(realSize.widthInt, realSize.heightInt)
        setPadding(rect.left.toInt(), rect.top.toInt(), rect.right.toInt(), rect.bottom.toInt())

        fitImageToView()
    }

    private fun fitImageToView() {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return
        }

        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        var scaleX = viewSize.width / drawableWidth
        var scaleY = viewSize.height / drawableHeight

        when (imageScaleType) {
            ScaleType.CENTER -> {
                scaleY = 1.0f
                scaleX = scaleY
            }
            ScaleType.CENTER_CROP -> {
                scaleY = max(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.CENTER_INSIDE -> {
                scaleY = min(1.0f, min(scaleX, scaleY))
                scaleX = scaleY
                scaleY = min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_CENTER -> {
                scaleY = min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_XY -> {
            }
            else -> throw UnsupportedOperationException("ZoomableImageView does not support FIT_START or FIT_END")
        }

        val redundantXSpace = viewSize.width - scaleX * drawableWidth
        val redundantYSpace = viewSize.height - scaleY * drawableHeight
        matchViewSize.width = viewSize.width - redundantXSpace
        matchViewSize.height = viewSize.height - redundantYSpace

        if ((!isZoomed && !imageRenderedAtLeastOnce)
            || (prevMatchViewSize.width == 0.0f && prevMatchViewSize.height == 0.0f)) {
            imgMatrix.setScale(scaleX, scaleY)
            imgMatrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            currentZoom = 1.0f
        } else {

            prevMatrix.getValues(matrix)

            matrix[Matrix.MSCALE_X] = matchViewSize.width / drawableWidth * currentZoom
            matrix[Matrix.MSCALE_Y] = matchViewSize.height / drawableHeight * currentZoom

            val transX = matrix[Matrix.MTRANS_X]
            val transY = matrix[Matrix.MTRANS_Y]

            val prevActualWidth = prevMatchViewSize.width * currentZoom
            val actualWidth = imageWidth
            translateMatrixAfterRotate(
                axis = Matrix.MTRANS_X,
                trans = transX,
                prevImageSize = prevActualWidth,
                imageSize = actualWidth,
                prevViewSize = prevViewSize.width,
                viewSize = viewSize.width,
                drawableSize = drawableWidth
            )

            val prevActualHeight = prevMatchViewSize.height * currentZoom
            val actualHeight = imageHeight
            translateMatrixAfterRotate(
                axis = Matrix.MTRANS_Y,
                trans = transY,
                prevImageSize = prevActualHeight,
                imageSize = actualHeight,
                prevViewSize = prevViewSize.height,
                viewSize = viewSize.height,
                drawableSize = drawableHeight
            )

            imgMatrix.setValues(matrix)
        }
        fixTrans()
        imageMatrix = imgMatrix
    }

    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> min(drawableWidth, size)
            MeasureSpec.UNSPECIFIED -> drawableWidth
            else -> size
        }
    }

    private fun translateMatrixAfterRotate(
        axis: Int,
        trans: Float,
        prevImageSize: Float,
        imageSize: Float,
        prevViewSize: Float,
        viewSize: Float,
        drawableSize: Int
    ) {
        when {
            imageSize < viewSize -> {
                matrix[axis] = (viewSize - drawableSize * matrix[Matrix.MSCALE_X]) * 0.5f
            }
            trans > 0 -> {
                matrix[axis] = -((imageSize - viewSize) / 2)
            }
            else -> {
                val percentage = (abs(trans) + prevViewSize / 2) / prevImageSize
                matrix[axis] = -(percentage * imageSize - viewSize / 2)
            }
        }
    }

    fun canScrollHorizontallyFroyo(direction: Int): Boolean {
        return canScrollHorizontally(direction)
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        imgMatrix.getValues(matrix)
        val x = matrix[Matrix.MTRANS_X]

        if (imageWidth < viewSize.width) {
            return false
        } else if (x >= -1 && direction < 0) {
            return false
        } else if (abs(x) + viewSize.width + 1f >= imageWidth && direction > 0) {
            return false
        }

        return true
    }

    fun getImageBounds(): RectF {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return RectF()
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        val realImageBounds = RectF()
        val imageBounds = RectF()
        realImageBounds.set(0.0f, 0.0f, drawableWidth.toFloat(), drawableHeight.toFloat())
        imageMatrix.mapRect(imageBounds, realImageBounds)
        return RectF(imageBounds)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        event.offsetLocation(-viewport.left, -viewport.top)
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        val curr = PointF(event.x, event.y)

        if (state == State.NONE || state == State.DRAG || state == State.FLING) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    requestDisallowInterceptTouchEvent(disallowIntercept = true)
                    lastMovePoint.set(curr)
                    fling?.cancelFling()
                    state = State.DRAG
                }
                MotionEvent.ACTION_MOVE -> if (state == State.DRAG) {
                    val deltaX = curr.x - lastMovePoint.x
                    val deltaY = curr.y - lastMovePoint.y
                    val fixTransX = getFixDragTrans(deltaX, viewSize.width, imageWidth)
                    val fixTransY = getFixDragTrans(deltaY, viewSize.height, imageHeight)
                    imgMatrix.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    lastMovePoint.set(curr.x, curr.y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    state = State.NONE
                    requestDisallowInterceptTouchEvent(false)
                }
            }
        }

        imageMatrix = imgMatrix

        userTouchListener?.onTouch(this, event)
        imageMoveListener?.onMove()
        return true
    }

    private fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            doubleTapListener?.let {
                return it.onSingleTapConfirmed(e)
            }
            return performClick()
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            fling?.cancelFling()
            fling = Fling(velocityX.toInt(), velocityY.toInt())
            fling?.let {
                compatPostOnAnimation(it)
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }

        override fun onDoubleTap(event: MotionEvent): Boolean {
            var consumed = doubleTapListener?.onDoubleTap(event) ?: false
            if (state == State.NONE) {
                val targetZoom = if (currentZoom == minScale) maxScale else minScale
                val doubleTap = DoubleTapZoom(targetZoom, focusX = event.x, focusY = event.y, stretchImageToSuper = false)
                compatPostOnAnimation(doubleTap)
                consumed = true
            }
            return consumed
        }

        override fun onDoubleTapEvent(e: MotionEvent): Boolean {
            return doubleTapListener?.onDoubleTapEvent(e) ?: false
        }
    }

    interface ImageMoveListener {

        fun onMove()

    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            state = State.ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleImage(detector.scaleFactor.toDouble(), detector.focusX, detector.focusY, true)
            imageMoveListener?.onMove()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            state = State.NONE
            var animateToZoomBoundary = false
            var targetZoom = currentZoom
            if (currentZoom > maxScale) {
                targetZoom = maxScale
                animateToZoomBoundary = true
            } else if (currentZoom < minScale) {
                targetZoom = minScale
                animateToZoomBoundary = true
            }

            if (animateToZoomBoundary) {
                val doubleTap = DoubleTapZoom(targetZoom, focusX = viewSize.width / 2, focusY = viewSize.height / 2, stretchImageToSuper = true)
                compatPostOnAnimation(doubleTap)
            }
        }
    }

    private fun scaleImage(deltaScale: Double, focusX: Float, focusY: Float, stretchImageToSuper: Boolean) {
        var scale = deltaScale

        val lowerScale: Float
        val upperScale: Float
        if (stretchImageToSuper) {
            lowerScale = superMinScale
            upperScale = superMaxScale
        } else {
            lowerScale = minScale
            upperScale = maxScale
        }

        val origScale = currentZoom
        currentZoom *= scale.toFloat()
        if (currentZoom > upperScale) {
            currentZoom = upperScale
            scale = (upperScale / origScale).toDouble()
        } else if (currentZoom < lowerScale) {
            currentZoom = lowerScale
            scale = (lowerScale / origScale).toDouble()
        }

        imgMatrix.postScale(scale.toFloat(), scale.toFloat(), focusX, focusY)
        fixScaleTrans()
    }

    private inner class DoubleTapZoom(
        private val targetZoom: Float,
        focusX: Float,
        focusY: Float,
        private val stretchImageToSuper: Boolean
    ) : Runnable {

        private val startTime: Long
        private val startZoom: Float
        private val bitmapX: Float
        private val bitmapY: Float
        private val interpolator = AccelerateDecelerateInterpolator()
        private val startTouch: PointF
        private val endTouch: PointF

        init {
            state = State.ANIMATE_ZOOM
            startTime = System.currentTimeMillis()
            this.startZoom = currentZoom
            val bitmapPoint = transformCoordTouchToBitmap(focusX, focusY, clipToBitmap = false)
            this.bitmapX = bitmapPoint.x
            this.bitmapY = bitmapPoint.y

            startTouch = transformCoordBitmapToTouch(bitmapX, bitmapY)
            endTouch = PointF(viewSize.width / 2, viewSize.height / 2)
        }

        override fun run() {
            val t = interpolate()
            val deltaScale = calculateDeltaScale(t)
            scaleImage(deltaScale, bitmapX, bitmapY, stretchImageToSuper)
            translateImageToCenterTouchPosition(t)
            fixScaleTrans()
            imageMatrix = imgMatrix

            imageMoveListener?.onMove()

            if (t < 1.0f) {
                compatPostOnAnimation(runnable = this)
            } else {
                state = State.NONE
            }
        }

        private fun translateImageToCenterTouchPosition(t: Float) {
            val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
            val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
            val curr = transformCoordBitmapToTouch(bitmapX, bitmapY)
            imgMatrix.postTranslate(targetX - curr.x, targetY - curr.y)
        }

        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime) / ZOOM_TIME
            elapsed = min(1f, elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom + t * (targetZoom - startZoom)).toDouble()
            return zoom / currentZoom
        }

    }

    private fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        imgMatrix.getValues(matrix)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val transX = matrix[Matrix.MTRANS_X]
        val transY = matrix[Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / imageWidth
        var finalY = (y - transY) * origH / imageHeight

        if (clipToBitmap) {
            finalX = min(max(finalX, 0f), origW)
            finalY = min(max(finalY, 0f), origH)
        }

        return PointF(finalX, finalY)
    }

    private fun transformCoordBitmapToTouch(bx: Float, by: Float): PointF {
        imgMatrix.getValues(matrix)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val px = bx / origW
        val py = by / origH
        val finalX = matrix[Matrix.MTRANS_X] + imageWidth * px
        val finalY = matrix[Matrix.MTRANS_Y] + imageHeight * py
        return PointF(finalX, finalY)
    }

    private inner class Fling(velocityX: Int, velocityY: Int) : Runnable {

        var scroller: CompatScroller? = null
        var currX: Int = 0
        var currY: Int = 0

        init {
            state = State.FLING
            scroller = CompatScroller(context)
            imgMatrix.getValues(matrix)

            val startX = matrix[Matrix.MTRANS_X].toInt()
            val startY = matrix[Matrix.MTRANS_Y].toInt()
            val minX: Int
            val maxX: Int
            val minY: Int
            val maxY: Int

            if (imageWidth > viewSize.width) {
                minX = (viewSize.width - imageWidth).toInt()
                maxX = 0
            } else {
                maxX = startX
                minX = maxX
            }

            if (imageHeight > viewSize.height) {
                minY = (viewSize.height - imageHeight).toInt()
                maxY = 0
            } else {
                maxY = startY
                minY = maxY
            }

            scroller?.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
            currX = startX
            currY = startY
        }

        fun cancelFling() {
            if (scroller != null) {
                state = State.NONE
                scroller?.forceFinished(finished = true)
            }
        }

        override fun run() {
            imageMoveListener?.onMove()

            if (scroller?.isFinished == true) {
                scroller = null
                return
            }

            scroller?.let { scroller ->
                if (scroller.computeScrollOffset()) {
                    val newX = scroller.currX
                    val newY = scroller.currY
                    val transX = newX - currX
                    val transY = newY - currY
                    currX = newX
                    currY = newY
                    imgMatrix.postTranslate(transX.toFloat(), transY.toFloat())
                    fixTrans()
                    imageMatrix = imgMatrix
                    compatPostOnAnimation(runnable = this)
                }
            }
        }
    }

    @TargetApi(VERSION_CODES.GINGERBREAD)
    private inner class CompatScroller(context: Context) {

        lateinit var scroller: Scroller
        var overScroller: OverScroller
        var isPreGingerbread: Boolean = false

        val currX: Int
            get() {
                return if (isPreGingerbread) {
                    scroller.currX
                } else {
                    overScroller.currX
                }
            }

        val currY: Int
            get() {
                return if (isPreGingerbread) {
                    scroller.currY
                } else {
                    overScroller.currY
                }
            }

        init {
            isPreGingerbread = false
            overScroller = OverScroller(context)
        }

        fun fling(startX: Int, startY: Int, velocityX: Int, velocityY: Int, minX: Int, maxX: Int, minY: Int, maxY: Int) {
            if (isPreGingerbread) {
                scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
            } else {
                overScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY)
            }
        }

        fun forceFinished(finished: Boolean) {
            if (isPreGingerbread) {
                scroller.forceFinished(finished)
            } else {
                overScroller.forceFinished(finished)
            }
        }

        val isFinished: Boolean
            get() {
                return if (isPreGingerbread) {
                    scroller.isFinished
                } else {
                    overScroller.isFinished
                }
            }

        fun computeScrollOffset(): Boolean {
            return if (isPreGingerbread) {
                scroller.computeScrollOffset()
            } else {
                overScroller.computeScrollOffset()
                overScroller.computeScrollOffset()
            }
        }
    }

    private fun compatPostOnAnimation(runnable: Runnable) {
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            postOnAnimation(runnable)
        } else {
            postDelayed(runnable, (1000 / 60).toLong())
        }
    }

    private inner class ZoomVariables(
        var scale: Float,
        var focusX: Float,
        var focusY: Float,
        var scaleType: ScaleType
    )

    private fun printMatrixInfo() {
        val n = FloatArray(9)
        imgMatrix.getValues(n)
    }

    class SavedState : BaseSavedState {

        val currentZoom: Float
        val matrix: FloatArray
        val prevMatchViewSize: SizeF
        val prevViewSize: SizeF
        val imageRenderedAtLeastOnce: Boolean

        constructor(
            superState: Parcelable,
            currentZoom: Float,
            matrix: FloatArray,
            prevMatchViewSize: SizeF,
            prevViewSize: SizeF,
            imageRenderedAtLeastOnce: Boolean
        ) : super(superState) {
            this.currentZoom = currentZoom
            this.matrix = matrix
            this.prevMatchViewSize = prevMatchViewSize
            this.prevViewSize = prevViewSize
            this.imageRenderedAtLeastOnce = imageRenderedAtLeastOnce
        }

        constructor(source: Parcel) : super(source) {
            currentZoom = source.readFloat()
            matrix = source.createFloatArray()!!
            prevMatchViewSize = source.readParcelable(SizeF::class.java.classLoader)!!
            prevViewSize = source.readParcelable(SizeF::class.java.classLoader)!!
            imageRenderedAtLeastOnce = (source.readInt() == 1)
        }

        companion object {

            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls<SavedState?>(size)
                }
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            with(out) {
                writeFloat(currentZoom)
                writeFloatArray(matrix)
                writeParcelable(prevMatchViewSize, flags)
                writeParcelable(prevViewSize, flags)
                writeInt(if (imageRenderedAtLeastOnce) 1 else 0)
            }
        }
    }

}

private const val SUPER_MIN_MULTIPLIER = .75f
private const val SUPER_MAX_MULTIPLIER = 1.25f
private const val ZOOM_TIME = 300f
