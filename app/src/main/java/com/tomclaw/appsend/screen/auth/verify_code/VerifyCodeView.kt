package com.tomclaw.appsend.screen.auth.verify_code

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable

interface VerifyCodeView {

    fun showProgress()

    fun showContent()

    fun showError()

    fun hideError()

    fun navigationClicks(): Observable<Unit>

}

class VerifyCodeViewImpl(view: View) : VerifyCodeView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)

    private val navigationRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
    }

    override fun showProgress() {
    }

    override fun showContent() {
    }

    override fun showError() {
    }

    override fun hideError() {
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

}
