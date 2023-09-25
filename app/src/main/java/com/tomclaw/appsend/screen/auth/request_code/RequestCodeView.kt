package com.tomclaw.appsend.screen.auth.request_code

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

interface RequestCodeView {

    fun showProgress()

    fun showContent()

    fun setEmail(value: String)

    fun enableSubmitButton()

    fun disableSubmitButton()

    fun showError(text: String)

    fun navigationClicks(): Observable<Unit>

    fun emailChanged(): Observable<String>

    fun submitClicks(): Observable<Unit>

}

class RequestCodeViewImpl(private val view: View) : RequestCodeView {

    private val rootView: View = view.findViewById(R.id.root_view)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val email: EditText = view.findViewById(R.id.email_input)
    private val submitButton: Button = view.findViewById(R.id.submit_button)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val emailChangedRelay = PublishRelay.create<String>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.your_email)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                emailChangedRelay.accept(s.toString())
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

    override fun setEmail(value: String) {
        email.setText(value)
    }

    override fun enableSubmitButton() {
        submitButton.isEnabled = true
    }

    override fun disableSubmitButton() {
        submitButton.isEnabled = false
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun emailChanged(): Observable<String> = emailChangedRelay

    override fun submitClicks(): Observable<Unit> = submitRelay

    override fun showError(text: String) {
        Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show()
    }

}
