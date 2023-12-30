package com.tomclaw.appsend.screen.gallery

import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface GalleryView {

    fun setTitle(title: String)

    fun showProgress()

    fun showContent()

    fun navigationClicks(): Observable<Unit>

}

class GalleryViewImpl(view: View) : GalleryView {

    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val pager: ViewPager2 = view.findViewById(R.id.pager)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)

    private val navigationRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
    }

    override fun setTitle(title: String) {
        toolbar.title = title
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

}