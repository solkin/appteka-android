package com.tomclaw.appsend.screen.unlink

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface UnlinkView {

    fun showProgress()

    fun showContent()

    fun setReason(reason: String)

    fun showUnlinkFailed()

    fun navigationClicks(): Observable<Unit>

    fun reasonChanged(): Observable<String>

    fun submitClicks(): Observable<Unit>

}

class UnlinkViewImpl(
    view: View,
    title: String
) : UnlinkView {

    private val context = view.context
    private val scrollView: View = view.findViewById(R.id.scroll_view)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val reasonEdit: EditText = view.findViewById(R.id.reason_input)
    private val submitButton: Button = view.findViewById(R.id.unlink_button)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val reasonChangedRelay = PublishRelay.create<String>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.setTitle(context.getString(R.string.unlink_of, title))
        reasonEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                reasonChangedRelay.accept(s.toString())
            }
        })
        submitButton.setOnClickListener { submitRelay.accept(Unit) }
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun setReason(reason: String) {
        reasonEdit.setText(reason)
    }

    override fun showUnlinkFailed() {
        Snackbar.make(
            scrollView,
            R.string.unable_to_unlink_file,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun reasonChanged(): Observable<String> = reasonChangedRelay

    override fun submitClicks(): Observable<Unit> = submitRelay

}
