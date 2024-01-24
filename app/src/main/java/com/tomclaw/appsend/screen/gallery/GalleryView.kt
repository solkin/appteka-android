package com.tomclaw.appsend.screen.gallery

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.viewpager2.widget.ViewPager2
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable

interface GalleryView {

    fun setTitle(title: String)

    fun contentUpdated()

    fun setCurrentIndex(index: Int)

    fun navigationClicks(): Observable<Unit>

    fun pageChanged(): Observable<Int>

    fun downloadClicks(): Observable<Unit>

}

class GalleryViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : GalleryView {

    private val pager: ViewPager2 = view.findViewById(R.id.pager)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val pageRelay = PublishRelay.create<Int>()
    private val downloadRelay = PublishRelay.create<Unit>()

    private val callback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            pageRelay.accept(position)
        }
    }

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.inflateMenu(R.menu.gallery_menu)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.download -> {
                    downloadRelay.accept(Unit)
                    true
                }

                else -> false
            }
        }

        pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        pager.adapter = adapter
    }

    override fun setTitle(title: String) {
        toolbar.title = title
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun setCurrentIndex(index: Int) {
        pager.setCurrentItem(index, false)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun pageChanged(): Observable<Int> = pageRelay
        .doOnSubscribe {
            if (!pageRelay.hasObservers()) {
                pager.registerOnPageChangeCallback(callback)
            }
        }
        .doFinally {
            if (!pageRelay.hasObservers()) {
                pager.unregisterOnPageChangeCallback(callback)
            }
        }

    override fun downloadClicks(): Observable<Unit> = downloadRelay

}
