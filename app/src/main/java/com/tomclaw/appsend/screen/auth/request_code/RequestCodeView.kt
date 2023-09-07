package com.tomclaw.appsend.screen.auth.request_code

import android.annotation.SuppressLint
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface RequestCodeView {

    fun showProgress()

    fun showContent()

    fun showError()

    fun hideError()

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

}

class RequestCodeViewImpl(view: View) : RequestCodeView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val recycler: RecyclerView = view.findViewById(R.id.recycler)
    private val error: View = view.findViewById(R.id.error)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)
    private val retryButton: View = view.findViewById(R.id.retry_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        retryButton.setOnClickListener { retryRelay.accept(Unit) }
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun showError() {
        error.show()
    }

    override fun hideError() {
        error.hide()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

}

private const val DURATION_MEDIUM = 300L
