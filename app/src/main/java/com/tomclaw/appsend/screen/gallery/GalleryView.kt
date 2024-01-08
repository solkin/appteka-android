package com.tomclaw.appsend.screen.gallery

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.WeakHashMap

interface GalleryView {

    fun setTitle(title: String)

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun setCurrentIndex(index: Int)

    fun navigationClicks(): Observable<Unit>

    fun activeChanged(): Observable<Int>

}

class GalleryViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : GalleryView {

    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val pager: ViewPager2 = view.findViewById(R.id.pager)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val activeRelay = PublishRelay.create<Int>()

    private val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            activeRelay.accept(position)
        }
    }

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        pager.adapter = adapter
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

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun setCurrentIndex(index: Int) {
        pager.currentItem = index
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun activeChanged(): Observable<Int> = activeRelay
        .doOnSubscribe {
            if (!activeRelay.hasObservers()) {
                pager.registerOnPageChangeCallback(callback)
            }
        }
        .doFinally {
            if (!activeRelay.hasObservers()) {
                pager.unregisterOnPageChangeCallback(callback)
            }
        }

}
