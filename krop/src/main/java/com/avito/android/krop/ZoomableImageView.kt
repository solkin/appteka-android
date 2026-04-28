package com.avito.android.krop

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
import android.widget.ImageView
import android.widget.OverScroller
import android.widget.Scroller
import com.avito.android.krop.util.KLine
import com.avito.android.krop.util.KPoint
import com.avito.android.krop.util.KRect
import com.avito.android.krop.util.ScaleAfterRotationStyle
import com.avito.android.krop.util.SizeF
import com.avito.android.krop.util.KropTransformation
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin

/*
 * ZoomableImageView allows you to zoom and move bitmap on view with touch effects
 * Based on TouchImageView by Michael Ortiz
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class ZoomableImageView : ImageView, ViewportUpdateListener {

    private enum class State {
        NONE, DRAG, ZOOM, FLING, ANIMATE_ZOOM
    }

    //
    // Matrix applied to image. MSCALE_X and MSCALE_Y should always be equal.
    // MTRANS_X and MTRANS_Y are the other values used. prevMatrix is the matrix
    // saved prior to the screen rotating.
    //
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
    private var delayedTransformation: KropTransformation? = null

    //
    // Size of view and previous view size (ie before rotation)
    //
    private var viewSize = SizeF() // Viewport size
    private var prevViewSize = SizeF()

    //
    // Size of image when it is stretched to fit view. Before and After rotation.
    //
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

    private lateinit var viewport: RectF

    /**
     * Return the point at the center of the zoomed image. The PointF coordinates range
     * in value between 0 and 1 and the focus point is denoted as a fraction from the left
     * and top of the view. For example, the top left corner of the image would be (0, 0).
     * And the bottom right corner would be (1, 1).
     *
     * @return PointF representing the scroll position of the zoomed image.
     */
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

    internal var rotationAngle: Float = NO_ROTATION_ANGLE
        private set

    //
    // Scale of image ranges from minScale to maxScale, where minScale == 1
    // when the image is stretched to fit view.
    //
    var currentZoom: Float = 0.0f
        private set

    /**
     * Returns false if image is in initial, unzoomed state. False, otherwise.
     *
     * @return true if image is zoomed
     */
    val isZoomed: Boolean
        get() = currentZoom != 1.0f

    /**
     * @return rect representing zoomed image
     */
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
        currentZoom = DEFAULT_MIN_ZOOM
        imageScaleType = ScaleType.CENTER_CROP
        minScale = DEFAULT_MIN_ZOOM
        maxScale = DEFAULT_MAX_ZOOM
        superMinScale = SUPER_MIN_MULTIPLIER * minScale
        superMaxScale = SUPER_MAX_MULTIPLIER * maxScale
        renderChanges()
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

    /**
     * Save the current matrix and view dimensions
     * in the prevMatrix and prevView variables.
     */
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
        val superState = super.onSaveInstanceState() ?: BaseSavedState.EMPTY_STATE
        imgMatrix.getValues(matrix)
        return SavedState(
                superState,
                currentZoom,
                rotationAngle,
                maxZoom,
                minZoom,
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
            rotationAngle = state.rotationAngle
            maxZoom = state.maxZoom
            minZoom = state.minZoom
            matrix = state.matrix
            prevMatchViewSize = state.prevMatchViewSize
            prevViewSize = state.prevViewSize
            imageRenderedAtLeastOnce = state.imageRenderedAtLeastOnce

            prevMatrix.setValues(matrix)
        } else {
            super.onRestoreInstanceState(state)
        }
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
        val realWidth = setViewSize(widthMode, widthSize, drawableWidth)
        val realHeight = setViewSize(heightMode, heightSize, drawableHeight)

        setMeasuredDimension(realWidth, realHeight)
    }

    override fun onUpdateViewport(newViewport: RectF) {
        viewport = newViewport

        viewSize.width = viewport.width()
        viewSize.height = viewport.height()

        val paddingX = viewport.left.toInt()
        val paddingY = viewport.top.toInt()
        setPadding(paddingX, paddingY, paddingX, paddingY)

        fitImageToView()
    }

    override fun onDraw(canvas: Canvas) {
        onDrawReady = true
        imageRenderedAtLeastOnce = true
        delayedZoomVariables?.let {
            setZoom(it.scale, it.focusX, it.focusY, it.scaleType)
            delayedZoomVariables = null
        }
        delayedTransformation?.let {
            setTransformation(it)
            delayedTransformation = null
        }
        super.onDraw(canvas)
    }

    public override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        savePreviousImageValues()
    }

    /**
     * Reset zoom and translation to initial state.
     */
    fun resetZoom() {
        currentZoom = DEFAULT_MIN_ZOOM
        fitImageToView()
    }

    /**
     * Set zoom to the specified scale. Image will be centered around the point
     * (focusX, focusY). These floats range from 0 to 1 and denote the focus point
     * as a fraction from the left and top of the view. For example, the top left
     * corner of the image would be (0, 0). And the bottom right corner would be (1, 1).
     */
    @JvmOverloads
    fun setZoom(scale: Float,
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
        renderChanges()
    }

    /**
     * @param angle clockwise angle for rotation
     * @param scaleAnimation style of the free space closing animation after rotation
     */
    fun rotateBy(angle: Float, scaleAnimation: ScaleAfterRotationStyle) {

        if (angle == NO_ROTATION_ANGLE) return

        val bounds = getCurrentBounds()?.center() ?: return
        val centeredMatrix = Matrix(imgMatrix).apply {
            postTranslate(-bounds.x, -bounds.y)
        }
        centeredMatrix.postRotate(angle)
        val correction = getBounds(centeredMatrix)!!.center()

        imgMatrix.postTranslate(-viewport.centerX(), -viewport.centerY())
        imgMatrix.postRotate(angle)
        imgMatrix.postTranslate(
                viewport.centerX() - correction.x,
                viewport.centerY() - correction.y
        )
        rotationAngle = (rotationAngle + angle) % 360

        val scaleStrategy: ((Float) -> Unit)? = when (scaleAnimation) {
            ScaleAfterRotationStyle.INSTANT -> { deltaScale ->
                scaleImage(deltaScale.toDouble(), viewSize.width / 2, viewSize.height / 2, stretchImageToSuper = false)
                fixTrans()
                minZoom = currentZoom
            }
            ScaleAfterRotationStyle.ANIMATE -> { deltaScale ->
                val targetScale = currentZoom * deltaScale
                val targetZoom = min(maxScale, targetScale)
                compatPostOnAnimation(DoubleTapZoom(
                        targetZoom,
                        focusX = viewSize.width / 2,
                        focusY = viewSize.height / 2,
                        stretchImageToSuper = true,
                        duration = UPSCALE_ON_ROTATION_TIME_MS,
                        fixTransOnScale = false
                ))
                minZoom = targetZoom
            }
            ScaleAfterRotationStyle.NONE -> null
        }
        scaleStrategy?.let { fixZoomAfterRotation(it) }
        renderChanges()

        imageMoveListener?.onMove()
    }

    /**
     * Set zoom parameters equal to another TouchImageView. Including scale, position,
     * and ScaleType.
     */
    fun setZoom(img: ZoomableImageView) {
        img.scrollPosition?.let { center ->
            setZoom(img.currentZoom, center.x, center.y, img.scaleType)
        }
    }

    /**
     * Set the focus point of the zoomed image. The focus points are denoted as a fraction from the
     * left and top of the view. The focus points can range in value between 0 and 1.
     */
    fun setScrollPosition(focusX: Float, focusY: Float) {
        setZoom(currentZoom, focusX, focusY)
    }

    fun getTransformation(): KropTransformation {
        val offset = getFocusOffset()

        return KropTransformation(
                scale = currentZoom,
                focusOffset = PointF(offset.x, offset.y),
                rotationAngle = rotationAngle
        )
    }

    fun setTransformation(transformation: KropTransformation) {
        if (!onDrawReady) {
            delayedTransformation = transformation
            return
        }

        with(transformation) {
            setZoom(scale = scale)
            rotateBy(rotationAngle, ScaleAfterRotationStyle.INSTANT)
            moveFocusBy(focusOffset.x, focusOffset.y)
        }

    }

    private fun moveFocusBy(dx: Float, dy: Float) {
        imgMatrix.postTranslate(dx, dy)
        renderChanges()
    }

    private fun fixZoomAfterRotation(scaleStrategy: (Float) -> Unit) {
        val image = getCurrentBounds() ?: return

        // No need to zoom in, if viewport already inside image
        if (image.contains(viewport.toKRect())) {
            tryFixMinScale()
            return
        }

        val viewportCenter = KPoint(viewport.centerX(), viewport.centerY())
        val diagonals = listOf(
                KLine(viewportCenter, KPoint(viewport.left, viewport.top)),
                KLine(viewportCenter, KPoint(viewport.left, viewport.bottom)),
                KLine(viewportCenter, KPoint(viewport.right, viewport.top)),
                KLine(viewportCenter, KPoint(viewport.right, viewport.bottom))
        )

        val borders = image.clockwiseBorders()
        val intersections = mutableListOf<Pair<KPoint, KLine>>()

        // If any viewport diagonal has intersection with image's border, we need an upscaling then
        diagonals.forEach { diag ->
            borders.forEach { border ->
                diag.findIntersection(border)?.let {
                    intersections.add(it to diag)
                }
            }
        }

        val scale = intersections.map { (intersection, diag) ->
            val shortenDiag = KLine(viewportCenter, intersection)
            diag.length() / shortenDiag.length()
        }.max()?.takeIf { it > UPSCALING_ROTATION_THRESHOLD }
        scale?.let { deltaScale -> scaleStrategy(deltaScale) }
    }

    private fun tryFixMinScale() {
        val image = getCurrentBounds() ?: return

        if (!image.contains(viewport.toKRect())) return

        fun KLine.getVectorDistantPoint(): KPoint {
            val distantPoint = max(imageWidth, imageHeight) // some max value to be out of image bounds
            val dx = p2.x - p1.x
            if (dx == 0f) return KPoint(p1.x, distantPoint) // fallback, we don't expect it
            val k = (p2.y - p1.y) / dx
            val newX = distantPoint * sign(dx)
            val newY = newX * k
            return KPoint(p1.x + newX, p1.y + newY)
        }

        val viewportCenter = KPoint(viewport.centerX(), viewport.centerY())
        val diagonals = listOf(
                KLine(viewportCenter, KPoint(viewport.left, viewport.top)),
                KLine(viewportCenter, KPoint(viewport.left, viewport.bottom)),
                KLine(viewportCenter, KPoint(viewport.right, viewport.top)),
                KLine(viewportCenter, KPoint(viewport.right, viewport.bottom))
        ).map { it.p2 to KLine(it.p1, it.getVectorDistantPoint()) } // convert them into outgoing vectors

        val borders = image.clockwiseBorders()
        val intersections = mutableListOf<Pair<KPoint, KLine>>()

        diagonals.forEach { (height, diag) ->
            borders.forEach { border ->
                diag.findIntersection(border)?.let {
                    intersections.add(height to KLine(viewportCenter, it))
                }
            }
        }

        // Iterate over intersections with image's border, might we can lower minScale
        val scale = intersections.map { (height, diag) ->
            val shortenDiag = KLine(viewportCenter, height)
            diag.length() / shortenDiag.length()
        }.min()?.takeIf { it > UPSCALING_ROTATION_THRESHOLD }
        scale?.let {
            val newMinScale = max(currentZoom / scale, DEFAULT_MIN_ZOOM)
            minZoom = newMinScale
        }
    }

    private fun removeExtraTrans() {
        imgMatrix.getValues(matrix)
        val transX = matrix[Matrix.MTRANS_X]
        val transY = matrix[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewSize.width, imageWidth)
        val fixTransY = getFixTrans(transY, viewSize.height, imageHeight)

        if (fixTransX != 0.0f || fixTransY != 0.0f) {
            imgMatrix.postTranslate(fixTransX, fixTransY)
        }
    }

    /**
     * Performs boundary checking and fixes the image matrix if it is out of bounds.
     */
    private fun fixTrans() {

        if (rotationAngle == NO_ROTATION_ANGLE) { // just use simple method
            removeExtraTrans()
        } else {
            val image = getCurrentBounds() ?: return
            val viewportRect = viewport.toKRect()
            if (image.contains(viewportRect)) return // viewport inside image, no need to fix

            val viewportCenter = viewportRect.center()
            val imageCenter = image.center()
            val centeredPort = viewportRect
                    .moveBy(imageCenter.x - viewportCenter.x, imageCenter.y - viewportCenter.y)

            val borders = image.clockwiseBorders()

            val viewportHeights = centeredPort.clockwiseHeights()
            // We need a start point for rectangle traverse
            // Use any of sides to find nearest point
            val nearestVertical = viewportHeights.map { borders.last().normalFrom(it) }.minBy { it.length() }!!

            // Rotate heights to support clockwise order of borders
            val nextNearestIndex = viewportHeights.indexOf(nearestVertical.p1) + 1
            var heightsQueue = mutableListOf<KPoint>()
            if (nextNearestIndex != viewportHeights.size) {
                heightsQueue.addAll(viewportHeights.subList(nextNearestIndex, viewportHeights.size))
            }
            heightsQueue.addAll(viewportHeights.subList(0, nextNearestIndex))

            check(heightsQueue.size == borders.size) { "Expected borders and heights sets to be same size" }

            fun KPoint.followLine(vector: KLine): KPoint {
                val (dx, dy) = vector.getTransition()
                return moveBy(dx, dy)
            }

            var tempCenter = centeredPort.center().followLine(nearestVertical)
            heightsQueue = heightsQueue.map { it.followLine(nearestVertical) }.toMutableList()

            val availabilityBorders = mutableListOf<KLine>()

            // Iterate on borders: forming "areas of gravity" for viewport center
            var lastPoint: KPoint? = null
            borders.forEach { border ->
                val height = heightsQueue.removeAt(0)
                val normal = border.normalFrom(height)

                tempCenter = tempCenter.followLine(normal)
                heightsQueue = heightsQueue.map { it.followLine(normal) }.toMutableList()

                lastPoint?.let {
                    availabilityBorders.add(KLine(it, tempCenter))
                }
                lastPoint = tempCenter
            }
            lastPoint?.let { availabilityBorders.add(KLine(it, availabilityBorders.first().p1)) }

            // Find nearest point to go for
            val nearestPivot = availabilityBorders
                    .map { it.nearestPointFor(viewportCenter) }
                    .minBy { KLine(it, viewportCenter).length() }
            nearestPivot?.let {
                val dx = viewportCenter.x - it.x
                val dy = viewportCenter.y - it.y
                imgMatrix.postTranslate(dx, dy)
            }
        }
    }

    private fun RectF.toKRect() = KRect(
            leftTop = KPoint(left, top),
            rightTop = KPoint(right, top),
            leftBottom = KPoint(left, bottom),
            rightBottom = KPoint(right, bottom)
    )

    /**
     * Returns offset required to get to focus point from image's center
     */
    private fun getFocusOffset(): KPoint {
        val imageCenter = getCurrentBounds()?.center()
                ?: KPoint(viewport.centerX(), viewport.centerY())
        val dx = imageCenter.x - viewport.centerX()
        val dy = imageCenter.y - viewport.centerY()
        return KPoint(dx, dy)
    }

    /**
     * When transitioning from zooming from focus to zoom from center (or vice versa)
     * the image can become unaligned within the view. This is apparent when zooming
     * quickly. When the content size is less than the view size, the content will often
     * be centered incorrectly within the view. fixScaleTrans first calls fixTrans() and
     * then makes sure the image is centered correctly within the view.
     */
    private fun fixScaleTrans() {
        fixTrans()
        // We do translations for rotated images in other place
        if (rotationAngle != NO_ROTATION_ANGLE) return
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

    /**
     * If the normalizedScale is equal to 1, then the image is made to fit the screen. Otherwise,
     * it is made to fit the screen according to the dimensions of the previous image matrix. This
     * allows the image to maintain its zoom after rotation.
     */
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
                scaleY = Math.max(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.CENTER_INSIDE -> {
                scaleY = Math.min(1.0f, Math.min(scaleX, scaleY))
                scaleX = scaleY
                scaleY = Math.min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_CENTER -> {
                scaleY = Math.min(scaleX, scaleY)
                scaleX = scaleY
            }
            ScaleType.FIT_XY -> {
            }
            else -> throw UnsupportedOperationException("ZoomableImageView does not support FIT_START or FIT_END")
        }

        // Center the image
        val redundantXSpace = viewSize.width - scaleX * drawableWidth
        val redundantYSpace = viewSize.height - scaleY * drawableHeight
        matchViewSize.width = viewSize.width - redundantXSpace
        matchViewSize.height = viewSize.height - redundantYSpace

        if ((!isZoomed && !imageRenderedAtLeastOnce)
                || (prevMatchViewSize.width == 0.0f && prevMatchViewSize.height == 0.0f)) {
            // Stretch and center image to fit view
            imgMatrix.setScale(scaleX, scaleY)
            imgMatrix.postTranslate(redundantXSpace / 2, redundantYSpace / 2)
            currentZoom = DEFAULT_MIN_ZOOM
            rotationAngle = NO_ROTATION_ANGLE
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
        renderChanges()
    }

    private fun setViewSize(mode: Int, size: Int, drawableWidth: Int): Int {
        return when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> Math.min(drawableWidth, size)
            MeasureSpec.UNSPECIFIED -> drawableWidth
            else -> size
        }
    }

    /**
     * After rotating, the matrix needs to be translated. This function finds the area of image
     * which was previously centered and adjusts translations so that is again the center, post-rotation.
     *
     * @param axis          Matrix.MTRANS_X or Matrix.MTRANS_Y
     * @param trans         the value of trans in that axis before the rotation
     * @param prevImageSize the width/height of the image before the rotation
     * @param imageSize     width/height of the image after rotation
     * @param prevViewSize  width/height of view before rotation
     * @param viewSize      width/height of view after rotation
     * @param drawableSize  width/height of drawable
     */
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
                // The width/height of image is less than the view's width/height. Center it.
                matrix[axis] = (viewSize - drawableSize * matrix[Matrix.MSCALE_X]) * 0.5f
            }
            trans > 0 -> {
                // The image is larger than the view, but was not before rotation. Center it.
                matrix[axis] = -((imageSize - viewSize) / 2)
            }
            else -> {
                // Find the area of the image which was previously centered in the view. Determine its distance
                // from the left/top side of the view as a fraction of the entire image's width/height. Use that percentage
                // to calculate the trans in the new view width/height.
                val percentage = (Math.abs(trans) + prevViewSize / 2) / prevImageSize
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

    internal fun getImageBounds(): RectF {
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

    private fun getDisplayedBounds() = getBounds(imageMatrix)

    // Returns bounds, stored in cache. May contain changes, that's not yet displayed
    private fun getCurrentBounds() = getBounds(imgMatrix)

    private fun getBounds(imageMatrix: Matrix): KRect? {
        val drawable = drawable
        if (drawable == null || drawable.intrinsicWidth == 0 || drawable.intrinsicHeight == 0) {
            return null
        }
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        //original image coords
        val points = floatArrayOf(
                0f, 0f,  //left, top
                drawableWidth.toFloat(), 0f,  //right, top
                drawableWidth.toFloat(), drawableHeight.toFloat(),  //right, bottom
                0f, drawableHeight.toFloat() //left, bottom
        )
        imageMatrix.mapPoints(points)
        return KRect(
                leftTop = KPoint(points[0] + paddingLeft, points[1] + paddingTop),
                rightTop = KPoint(points[2] + paddingRight, points[3] + paddingTop),
                rightBottom = KPoint(points[4] + paddingRight, points[5] + paddingBottom),
                leftBottom = KPoint(points[6] + paddingLeft, points[7] + paddingBottom)
        )
    }

    private fun renderChanges() {
        imageMatrix = imgMatrix
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
                    if (fixTransX != 0.0f || fixTransY != 0.0f) {
                        imgMatrix.postTranslate(fixTransX, fixTransY)
                        fixTrans()
                    }
                    lastMovePoint.set(curr.x, curr.y)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    state = State.NONE
                    requestDisallowInterceptTouchEvent(false)
                }
            }
        }

        renderChanges()

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

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            // If a previous fling is still active, it should be cancelled so that two flings
            // are not run simultaenously.
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
            tryFixMinScale()
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

    /**
     * DoubleTapZoom calls a series of runnables which apply
     * an animated zoom in/out graphic to the image.
     */
    private inner class DoubleTapZoom(
            private val targetZoom: Float,
            focusX: Float,
            focusY: Float,
            private val stretchImageToSuper: Boolean,
            private val duration: Float = ZOOM_TIME_MS,
            private val fixTransOnScale: Boolean = true
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

            // We consider some deltas insignificant.
            // In this case, we give penalty to subsequent image translation.
            // Inaccuracy in translations will be fixed on the last step
            val noScaleOnThisStep = abs(deltaScale - SCALE_NO_CHANGES_DELTA) < SCALE_EPS
            val partialImageTranslateToCenter = !fixTransOnScale && noScaleOnThisStep
            val translateWeight = if (partialImageTranslateToCenter) (1 - t) else 1f
            translateImageToCenterTouchPosition(t, translateWeight)

            val isFinished = isAnimationFinished(t)
            if (fixTransOnScale || partialImageTranslateToCenter && isFinished) {
                fixScaleTrans()
            }
            renderChanges()
            imageMoveListener?.onMove()

            if (isFinished) {
                // Finished zooming
                state = State.NONE
            } else {
                // We haven't finished zooming
                compatPostOnAnimation(runnable = this)
            }
        }

        private fun isAnimationFinished(t: Float) = t >= 1

        /**
         * Interpolate between where the image should start and end in order to translate
         * the image so that the point that is touched is what ends up centered at the end
         * of the zoom.
         */
        private fun translateImageToCenterTouchPosition(t: Float, translateWeight: Float) {
            val targetX = startTouch.x + t * (endTouch.x - startTouch.x)
            val targetY = startTouch.y + t * (endTouch.y - startTouch.y)
            val curr = transformCoordBitmapToTouch(bitmapX, bitmapY)
            val dx = (targetX - curr.x) * translateWeight
            val dy = (targetY - curr.y) * translateWeight
            imgMatrix.postTranslate(dx, dy)
        }

        private fun interpolate(): Float {
            val currTime = System.currentTimeMillis()
            var elapsed = (currTime - startTime) / duration
            elapsed = min(1f, elapsed)
            return interpolator.getInterpolation(elapsed)
        }

        /**
         * Interpolate the current targeted zoom and get the delta
         * from the current zoom.
         */
        private fun calculateDeltaScale(t: Float): Double {
            val zoom = (startZoom + t * (targetZoom - startZoom)).toDouble()
            return zoom / currentZoom
        }

    }

    /**
     * This function will transform the coordinates in the touch event to the coordinate
     * system of the drawable that the imageview contain
     *
     * @param x            x-coordinate of touch event
     * @param y            y-coordinate of touch event
     * @param clipToBitmap Touch event may occur within view, but outside image content. True, to clip return value
     *                     to the bounds of the bitmap size.
     * @return Coordinates of the point touched, in the coordinate system of the original drawable.
     */
    private fun transformCoordTouchToBitmap(x: Float, y: Float, clipToBitmap: Boolean): PointF {
        imgMatrix.getValues(matrix)
        val origW = drawable.intrinsicWidth.toFloat()
        val origH = drawable.intrinsicHeight.toFloat()
        val transX = matrix[Matrix.MTRANS_X]
        val transY = matrix[Matrix.MTRANS_Y]
        var finalX = (x - transX) * origW / imageWidth
        var finalY = (y - transY) * origH / imageHeight

        if (clipToBitmap) {
            finalX = Math.min(Math.max(finalX, 0f), origW)
            finalY = Math.min(Math.max(finalY, 0f), origH)
        }

        return PointF(finalX, finalY)
    }

    /**
     * Inverse of transformCoordTouchToBitmap. This function will transform the coordinates in the
     * drawable's coordinate system to the view's coordinate system.
     *
     * @param bx x-coordinate in original bitmap coordinate system
     * @param by y-coordinate in original bitmap coordinate system
     * @return Coordinates of the point in the view's coordinate system.
     */
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

    /**
     * Fling launches sequential runnables which apply
     * the fling graphic to the image. The values for the translation
     * are interpolated by the Scroller.
     */
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

            val sinalpha = sin(toRadians(rotationAngle.toDouble()))
            val cosalpha = cos(toRadians(rotationAngle.toDouble()))

            if (imageWidth > viewSize.width) {
                val widthWeight = -imageWidth * cosalpha
                val minWidthWeight = widthWeight.takeIf { cosalpha > 0 } ?: 0.0
                val maxWidthWeight = widthWeight.takeIf { cosalpha < 0 } ?: 0.0

                val heightWeight = imageHeight * sinalpha
                val minHeightWeight = heightWeight.takeIf { sinalpha < 0 } ?: 0.0
                val maxHeightWeight = heightWeight.takeIf { sinalpha > 0 } ?: 0.0

                minX = (viewSize.width + minWidthWeight + minHeightWeight).toInt()
                maxX = (imageWidth + maxWidthWeight + maxHeightWeight).toInt()
            } else {
                maxX = startX
                minX = maxX
            }

            if (imageHeight > viewSize.height) {
                val heightWeight = -imageHeight * cosalpha
                val minHeightWeight = heightWeight.takeIf { cosalpha > 0 } ?: 0.0
                val maxHeightWeight = heightWeight.takeIf { cosalpha < 0 } ?: 0.0

                val widthWeight = -imageWidth * sinalpha
                val minWidthWeight = widthWeight.takeIf { sinalpha > 0 } ?: 0.0
                val maxWidthWeight = widthWeight.takeIf { sinalpha < 0 } ?: 0.0

                minY = (viewSize.height + minWidthWeight + minHeightWeight).toInt()
                maxY = (imageHeight + maxWidthWeight + maxHeightWeight).toInt()
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
                    renderChanges()
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

    private data class ZoomVariables(
            val scale: Float,
            val focusX: Float,
            val focusY: Float,
            val scaleType: ScaleType
    )

    class SavedState : BaseSavedState {

        val currentZoom: Float
        val rotationAngle: Float
        val maxZoom: Float
        val minZoom: Float
        val matrix: FloatArray
        val prevMatchViewSize: SizeF
        val prevViewSize: SizeF
        val imageRenderedAtLeastOnce: Boolean

        constructor(superState: Parcelable,
                    currentZoom: Float,
                    rotationAngle: Float,
                    maxZoom: Float,
                    minZoom: Float,
                    matrix: FloatArray,
                    prevMatchViewSize: SizeF,
                    prevViewSize: SizeF,
                    imageRenderedAtLeastOnce: Boolean) : super(superState) {
            this.currentZoom = currentZoom
            this.rotationAngle = rotationAngle
            this.maxZoom = maxZoom
            this.minZoom = minZoom
            this.matrix = matrix
            this.prevMatchViewSize = prevMatchViewSize
            this.prevViewSize = prevViewSize
            this.imageRenderedAtLeastOnce = imageRenderedAtLeastOnce
        }

        constructor(source: Parcel) : super(source) {
            currentZoom = source.readFloat()
            rotationAngle = source.readFloat()
            maxZoom = source.readFloat()
            minZoom = source.readFloat()
            matrix = source.createFloatArray() ?: FloatArray(9)
            prevMatchViewSize = source.readParcelable<SizeF>(SizeF::class.java.classLoader)
                ?: SizeF()
            prevViewSize = source.readParcelable<SizeF>(SizeF::class.java.classLoader)
                ?: SizeF()
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
                writeFloat(rotationAngle)
                writeFloat(maxZoom)
                writeFloat(minZoom)
                writeFloatArray(matrix)
                writeParcelable(prevMatchViewSize, flags)
                writeParcelable(prevViewSize, flags)
                writeInt(if (imageRenderedAtLeastOnce) 1 else 0)
            }
        }
    }

}

private const val DEFAULT_MIN_ZOOM = 1f
private const val DEFAULT_MAX_ZOOM = 5f

//
// SuperMin and SuperMax multipliers. Determine how much the image can be
// zoomed below or above the zoom boundaries, before animating back to the
// min/max zoom boundary.
//
private const val SUPER_MIN_MULTIPLIER = .75f
private const val SUPER_MAX_MULTIPLIER = 1.25f

private const val ZOOM_TIME_MS = 300f
private const val UPSCALE_ON_ROTATION_TIME_MS = 200f
private const val SCALE_NO_CHANGES_DELTA = 1.0f
private const val SCALE_EPS = 0.001f
private const val UPSCALING_ROTATION_THRESHOLD = 1f
private const val NO_ROTATION_ANGLE = 0f