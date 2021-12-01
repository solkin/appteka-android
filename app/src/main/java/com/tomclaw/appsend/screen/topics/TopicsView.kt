package com.tomclaw.appsend.screen.topics

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable

interface TopicsView {

    fun showIntro()

    fun showProgress()

    fun showError()

    fun showContent()

    fun getStartedClicks(): Observable<Unit>

    fun retryButtonClicks(): Observable<Unit>

    fun contentUpdated()

    fun contentUpdated(position: Int)

}

class TopicsViewImpl(
    private val view: View,
    private val adapter: SimpleRecyclerAdapter
) : TopicsView {

    private val viewFlipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val getStartedButton: View = view.findViewById(R.id.get_started_button)
    private val retryButton: View = view.findViewById(R.id.button_retry)
    private val errorText: TextView = view.findViewById(R.id.error_text)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)

    private val getStartedRelay = PublishRelay.create<Unit>()
    private val retryButtonRelay = PublishRelay.create<Unit>()

    init {
        val orientation = RecyclerView.VERTICAL
        val layoutManager = LinearLayoutManager(view.context, orientation, false)
        adapter.setHasStableIds(true)
        recycler.adapter = adapter
        recycler.layoutManager = layoutManager
        recycler.itemAnimator = DefaultItemAnimator()
        recycler.itemAnimator?.changeDuration = DURATION_MEDIUM

        getStartedButton.setOnClickListener { getStartedRelay.accept(Unit) }
        retryButton.setOnClickListener { retryButtonRelay.accept(Unit) }
        errorText.setText(R.string.topics_loading_failed)
    }

    override fun showIntro() {
        viewFlipper.displayedChild = 0
    }

    override fun showProgress() {
        viewFlipper.displayedChild = 1
    }

    override fun showError() {
        viewFlipper.displayedChild = 2
    }

    override fun showContent() {
        viewFlipper.displayedChild = 3
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

    override fun getStartedClicks(): Observable<Unit> = getStartedRelay

    override fun retryButtonClicks(): Observable<Unit> = retryButtonRelay

}

private const val DURATION_MEDIUM = 300L
