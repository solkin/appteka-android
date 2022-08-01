package com.tomclaw.appsend.screen.details

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface DetailsView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showSnackbar(text: String)

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

}

class DetailsViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : DetailsView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val blockingProgress: View = view.findViewById(R.id.blocking_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()

    private val layoutManager: LinearLayoutManager

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        val orientation = RecyclerView.VERTICAL
        layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM
    }

    override fun showProgress() {
        blockingProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        blockingProgress.hideWithAlphaAnimation(animateFully = false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun showSnackbar(text: String) {
        Snackbar.make(recycler, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

}

private const val DURATION_MEDIUM = 300L
