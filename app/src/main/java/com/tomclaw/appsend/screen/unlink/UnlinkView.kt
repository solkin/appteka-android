package com.tomclaw.appsend.screen.unlink

import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface UnlinkView {

    fun showProgress()

    fun showContent()

    fun setTitle(title: String)

    fun navigationClicks(): Observable<Unit>

    fun submitClicks(): Observable<String>

}

class UnlinkViewImpl(
    view: View
) : UnlinkView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val reasonEdit: EditText = view.findViewById(R.id.reason_input)
    private val submitButton: Button = view.findViewById(R.id.unlink_button)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val submitRelay = PublishRelay.create<String>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        submitButton.setOnClickListener { submitRelay.accept(reasonEdit.text.toString()) }
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun setTitle(title: String) {
        toolbar.title = context.getString(R.string.unlink_of, title)
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun submitClicks(): Observable<String> = submitRelay

}
