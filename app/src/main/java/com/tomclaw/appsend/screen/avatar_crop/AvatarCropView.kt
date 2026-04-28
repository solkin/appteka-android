package com.tomclaw.appsend.screen.avatar_crop

import android.graphics.Bitmap
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.core.view.doOnLayout
import com.avito.android.krop.KropView
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface AvatarCropView {

    fun setBitmap(bitmap: Bitmap)

    /** Synchronously read the cropped bitmap out of the underlying KropView. */
    fun readCroppedBitmap(): Bitmap?

    fun showProgress()

    fun showContent()

    fun showError(text: String)

    fun navigationClicks(): Observable<Unit>

    fun doneClicks(): Observable<Unit>

}

class AvatarCropViewImpl(view: View) : AvatarCropView {

    private val rootView: View = view.findViewById(R.id.root_view)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val kropView: KropView = view.findViewById(R.id.krop_view)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val doneRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.avatar_crop_title)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.inflateMenu(R.menu.avatar_crop_menu)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.menu_done) {
                doneRelay.accept(Unit)
                true
            } else false
        }
    }

    override fun setBitmap(bitmap: Bitmap) {
        // Defer until the KropView's children have been laid out so the
        // overlay's onOverlayMeasured has fired and the inner
        // ZoomableImageView knows its viewport. Calling setBitmap before
        // that leaves the matrix degenerate (viewport size = 0) and the
        // bitmap ends up pinned to the top-left of the cropper area
        // instead of centered inside the crop window.
        kropView.doOnLayout {
            kropView.setBitmap(bitmap)
        }
    }

    override fun readCroppedBitmap(): Bitmap? = kropView.getCroppedBitmap()

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError(text: String) {
        Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay
    override fun doneClicks(): Observable<Unit> = doneRelay

}
