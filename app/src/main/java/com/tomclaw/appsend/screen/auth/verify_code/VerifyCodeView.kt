package com.tomclaw.appsend.screen.auth.verify_code

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface VerifyCodeView {

    fun showProgress()

    fun showContent()

    fun showNameInput()

    fun hideNameInput()

    fun showError()

    fun navigationClicks(): Observable<Unit>

}

class VerifyCodeViewImpl(private val view: View) : VerifyCodeView {

    private val context = view.context
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val codeInput: EditText = view.findViewById(R.id.name_block)
    private val nameBlock: View = view.findViewById(R.id.code_input)
    private val nameInput: EditText = view.findViewById(R.id.name_input)
    private val submitButton: Button = view.findViewById(R.id.submit_button)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val codeChangedRelay = PublishRelay.create<String>()
    private val nameChangedRelay = PublishRelay.create<String>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.verify_email)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        codeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                codeChangedRelay.accept(s.toString())
            }
        })
        nameInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                nameChangedRelay.accept(s.toString())
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

    override fun showNameInput() {
        nameBlock.show()
    }

    override fun hideNameInput() {
        nameBlock.hide()
    }

    override fun showError() {
        Snackbar.make(view, R.string.error_verifying_code, Snackbar.LENGTH_LONG).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

}
