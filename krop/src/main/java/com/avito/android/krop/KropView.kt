package com.avito.android.krop

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.annotation.WorkerThread
import com.avito.android.krop.util.BitmapTransformation
import com.avito.android.krop.util.KropTransformation
import com.avito.android.krop.util.ScaleAfterRotationStyle
import com.avito.android.krop.util.transformWith

class KropView(context: Context, attrs: AttributeSet) :
        FrameLayout(context, attrs), OverlayView.MeasureListener {

    private val viewport = RectF()

    private var offset = 0
    private var aspectX = 1
    private var aspectY = 1
    private var overlayColor = Color.TRANSPARENT
    private var overlayShape: Int = SHAPE_OVAL

    @IdRes
    private var overlayResId: Int = DEFAULT_OVERLAY_ID
    private var bitmap: Bitmap? = null

    private lateinit var imageView: ZoomableImageView
    private lateinit var overlayView: OverlayView

    var transformationListener: TransformationListener? = null

    init {
        parseAttrs(attrs)
        initViews(context)
    }

    private fun parseAttrs(attrs: AttributeSet) {
        var arr: TypedArray? = null
        try {
            arr = context.obtainStyledAttributes(attrs, R.styleable.KropView)
            with(arr) {
                offset = getDimensionPixelOffset(R.styleable.KropView_krop_offset, offset)
                aspectX = getInteger(R.styleable.KropView_krop_aspectX, aspectX)
                aspectY = getInteger(R.styleable.KropView_krop_aspectY, aspectY)
                overlayShape = getInteger(R.styleable.KropView_krop_shape, overlayShape)
                overlayColor = getColor(R.styleable.KropView_krop_overlayColor, overlayColor)
                overlayResId = getResourceId(R.styleable.KropView_krop_overlay, overlayResId)
            }
        } finally {
            arr?.recycle()
        }
    }

    private fun initViews(context: Context) {
        imageView = ZoomableImageView(context)
        imageView.imageMoveListener = object : ZoomableImageView.ImageMoveListener {
            override fun onMove() {
                transformationListener?.onUpdate(getTransformation())
            }
        }
        addView(imageView)

        applyOverlayShape(overlayShape)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (overlayResId != DEFAULT_OVERLAY_ID) {
            val overlay = rootView.findViewById(overlayResId) as? OverlayView
                    ?: error("Overlay should instantiate OverlayView class")
            applyOverlay(overlay)
        }
    }

    fun rotateBy(angle: Float, scaleAnimation: ScaleAfterRotationStyle = ScaleAfterRotationStyle.NONE) =
            imageView.rotateBy(angle, scaleAnimation)

    fun setZoom(scale: Float) {
        imageView.setZoom(scale)
    }

    fun setMaxScale(scale: Float) {
        imageView.maxZoom = scale
    }

    fun setMinScale(scale: Float) {
        imageView.minZoom = scale
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap
        imageView.setImageBitmap(bitmap)
    }

    fun setTransformation(transformation: KropTransformation) =
            imageView.setTransformation(transformation)

    fun getTransformation() = imageView.getTransformation()

    @WorkerThread
    fun getCroppedBitmap(): Bitmap? {
        val renderBitmap = bitmap ?: return null
        val transformation = getResultTransformation()
        return renderBitmap.transformWith(transformation)
    }

    fun getResultTransformation(): BitmapTransformation {
        val bitmap = bitmap ?: return BitmapTransformation()
        val bounds = imageView.getImageBounds()
        val multiplier = bitmap.width.toFloat() / bounds.width()
        val rect = RectF()
        with(rect) {
            left = (-bounds.left) * multiplier
            top = (-bounds.top) * multiplier
            right = (-bounds.left + viewport.width()) * multiplier
            bottom = (-bounds.top + viewport.height()) * multiplier
        }

        val matrix = Matrix(imageView.imageMatrix).apply { postScale(multiplier, multiplier) }

        return BitmapTransformation(
                matrix,
                inputSize = BitmapTransformation.Size(bitmap.width, bitmap.height),
                outputSize = BitmapTransformation.Size(rect.width().toInt(), rect.height().toInt()),
        )
    }

    override fun onOverlayMeasured() {
        val width = overlayView.measuredWidth
        val height = overlayView.measuredHeight
        calculateViewport(viewport, width, height, offset, aspectX, aspectY)

        overlayView.onUpdateViewport(viewport)

        // Both overlay and image are KropView children, so their .left/.top
        // are in KropView's local coordinate space — that's the only space
        // in which subtraction is meaningful. Upstream's `overlayView.left
        // - this.left` mixed KropView's local coords with KropView's
        // position inside its parent, which produced a phantom offset
        // whenever KropView wasn't pinned to (0, 0) of its parent (e.g.
        // when it sits below a toolbar in a LinearLayout).
        val imageViewport = RectF(viewport).also {
            val dx = overlayView.left - imageView.left
            val dy = overlayView.top - imageView.top
            it.offset(dx.toFloat(), dy.toFloat())
        }
        imageView.onUpdateViewport(imageViewport)
        imageView.requestLayout()
        invalidate()
    }

    fun applyOffset(offset: Int) {
        this.offset = offset
        overlayView.requestLayout()
        imageView.resetZoom()
        invalidate()
    }

    fun applyAspectRatio(aspectX: Int, aspectY: Int) {
        this.aspectX = aspectX
        this.aspectY = aspectY
        overlayView.requestLayout()
        imageView.resetZoom()
        invalidate()
    }

    fun applyOverlayColor(color: Int) {
        this.overlayColor = color
        overlayView.setOverlayColor(overlayColor)
        invalidate()
    }

    fun applyOverlayShape(@OverlayShape shape: Int) {
        overlayShape = shape
        overlayView = when (overlayShape) {
            SHAPE_OVAL -> OvalOverlay(context)
            else -> RectOverlay(context)
        }
        setupOverlayView()
    }

    fun applyOverlay(overlay: OverlayView) {
        overlayShape = SHAPE_CUSTOM
        overlayView = overlay
        setupOverlayView()
    }

    override fun invalidate() {
        imageView.invalidate()
        overlayView.invalidate()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState() ?: BaseSavedState.EMPTY_STATE
        return SavedState(
                superState = superState,
                offset = offset,
                aspectX = aspectX,
                aspectY = aspectY,
                overlayColor = overlayColor,
                overlayShape = overlayShape,
                imageViewState = imageView.onSaveInstanceState() ?: BaseSavedState.EMPTY_STATE
        )
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            offset = state.offset
            aspectX = state.aspectX
            aspectY = state.aspectY
            overlayColor = state.overlayColor
            overlayShape = state.overlayShape
            imageView.onRestoreInstanceState(state.imageViewState)
            overlayView.setOverlayColor(overlayColor)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun setupOverlayView() {
        overlayView.setOverlayColor(overlayColor)
        if (childCount > OVERLAY_HIERARCHY_INDEX) removeViewAt(OVERLAY_HIERARCHY_INDEX)
        if (overlayView.parent == null) {
            addView(overlayView, OVERLAY_HIERARCHY_INDEX)
        }
        overlayView.setMeasureListener(this)
    }

    private fun calculateViewport(rect: RectF, width: Int, height: Int, offset: Int, aspectX: Int, aspectY: Int): RectF {
        val x: Float = width * 0.5f
        val y: Float = height * 0.5f

        val maxWidth: Float = width - offset * 2f
        val maxHeight: Float = height - offset * 2f

        val desiredWidth: Float
        val desiredHeight: Float

        when {
            maxWidth < maxHeight -> {
                desiredWidth = maxWidth
                desiredHeight = maxWidth * aspectY / aspectX
            }
            maxWidth > maxHeight -> {
                desiredWidth = maxHeight * aspectX / aspectY
                desiredHeight = maxHeight
            }
            else -> {
                desiredWidth = maxWidth
                desiredHeight = maxHeight
            }
        }

        var resultWidth = maxWidth
        var resultHeight = maxWidth * desiredHeight / desiredWidth
        if (resultHeight > maxHeight) {
            resultHeight = maxHeight
            resultWidth = desiredWidth * maxHeight / desiredHeight
        }

        with(rect) {
            left = x - resultWidth / 2
            top = y - resultHeight / 2
            right = x + resultWidth / 2
            bottom = y + resultHeight / 2
        }

        return rect
    }

    interface TransformationListener {

        fun onUpdate(transformation: KropTransformation)

    }

    class SavedState : BaseSavedState {

        var offset: Int
        var aspectX: Int
        var aspectY: Int
        var overlayColor: Int
        var overlayShape: Int
        val imageViewState: Parcelable

        constructor(superState: Parcelable,
                    offset: Int,
                    aspectX: Int,
                    aspectY: Int,
                    overlayColor: Int,
                    @OverlayShape overlayShape: Int,
                    imageViewState: Parcelable) : super(superState) {
            this.offset = offset
            this.aspectX = aspectX
            this.aspectY = aspectY
            this.overlayColor = overlayColor
            this.overlayShape = overlayShape
            this.imageViewState = imageViewState
        }

        constructor(source: Parcel) : super(source) {
            offset = source.readInt()
            aspectX = source.readInt()
            aspectY = source.readInt()
            overlayColor = source.readInt()
            overlayShape = source.readInt()
            imageViewState = source.readParcelable<Parcelable>(SavedState::class.java.classLoader)
                ?: BaseSavedState.EMPTY_STATE
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls<SavedState?>(size)
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            with(out) {
                writeInt(offset)
                writeInt(aspectX)
                writeInt(aspectY)
                writeInt(overlayColor)
                writeInt(overlayShape)
                writeParcelable(imageViewState, flags)
            }
        }
    }
}

private const val DEFAULT_OVERLAY_ID = 0
private const val OVERLAY_HIERARCHY_INDEX = 1