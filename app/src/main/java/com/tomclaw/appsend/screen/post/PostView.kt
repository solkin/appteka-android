package com.tomclaw.appsend.screen.post

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable


interface PostView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun showPostError()

    fun showUnauthorizedError()

    fun navigationClicks(): Observable<Unit>

    fun loginClicks(): Observable<Unit>

}

class PostViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : PostView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val progress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val loginRelay = PublishRelay.create<Unit>()

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
        progress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        progress.hideWithAlphaAnimation(animateFully = false)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun showPostError() {
        Snackbar.make(recycler, R.string.feed_post_failed, Snackbar.LENGTH_SHORT).show()
    }

    override fun showUnauthorizedError() {
        Snackbar
            .make(recycler, R.string.authorization_required_message, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.login_button) {
                loginRelay.accept(Unit)
            }
            .show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun loginClicks(): Observable<Unit> = loginRelay

}

private const val DURATION_MEDIUM = 300L
