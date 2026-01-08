package com.tomclaw.appsend.screen.change_email

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface ChangeEmailView {

    fun showProgress()

    fun showContent()

    fun setEmail(value: String)

    fun setCode(value: String)

    fun enableSendCodeButton()

    fun disableSendCodeButton()

    fun enableConfirmButton()

    fun disableConfirmButton()

    fun showCodeSection(codeSentMessage: String)

    fun hideCodeSection()

    fun lockEmailInput()

    fun unlockEmailInput()

    fun showError(text: String)

    fun showSuccess(text: String)

    fun navigationClicks(): Observable<Unit>

    fun emailChanged(): Observable<String>

    fun codeChanged(): Observable<String>

    fun sendCodeClicks(): Observable<Unit>

    fun confirmClicks(): Observable<Unit>

}

class ChangeEmailViewImpl(private val view: View) : ChangeEmailView {

    private val rootView: View = view.findViewById(R.id.root_view)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val emailInput: EditText = view.findViewById(R.id.email_input)
    private val sendCodeButton: Button = view.findViewById(R.id.send_code_button)
    private val codeSection: View = view.findViewById(R.id.code_section)
    private val codeSentMessage: TextView = view.findViewById(R.id.code_sent_message)
    private val codeInput: EditText = view.findViewById(R.id.code_input)
    private val confirmButton: Button = view.findViewById(R.id.confirm_button)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val emailChangedRelay = PublishRelay.create<String>()
    private val codeChangedRelay = PublishRelay.create<String>()
    private val sendCodeRelay = PublishRelay.create<Unit>()
    private val confirmRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.change_email_title)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        emailInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                emailChangedRelay.accept(s.toString())
            }
        })

        codeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                codeChangedRelay.accept(s.toString())
            }
        })

        sendCodeButton.setOnClickListener { sendCodeRelay.accept(Unit) }
        confirmButton.setOnClickListener { confirmRelay.accept(Unit) }
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun setEmail(value: String) {
        emailInput.setText(value)
    }

    override fun setCode(value: String) {
        codeInput.setText(value)
    }

    override fun enableSendCodeButton() {
        sendCodeButton.isEnabled = true
    }

    override fun disableSendCodeButton() {
        sendCodeButton.isEnabled = false
    }

    override fun enableConfirmButton() {
        confirmButton.isEnabled = true
    }

    override fun disableConfirmButton() {
        confirmButton.isEnabled = false
    }

    override fun showCodeSection(codeSentMessage: String) {
        this.codeSentMessage.text = codeSentMessage
        codeSection.show()
        codeInput.requestFocus()
    }

    override fun hideCodeSection() {
        codeSection.hide()
    }

    override fun lockEmailInput() {
        emailInput.isEnabled = false
        sendCodeButton.hide()
    }

    override fun unlockEmailInput() {
        emailInput.isEnabled = true
        sendCodeButton.show()
    }

    override fun showError(text: String) {
        Snackbar.make(rootView, text, Snackbar.LENGTH_LONG).show()
    }

    override fun showSuccess(text: String) {
        Snackbar.make(rootView, text, Snackbar.LENGTH_SHORT).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun emailChanged(): Observable<String> = emailChangedRelay

    override fun codeChanged(): Observable<String> = codeChangedRelay

    override fun sendCodeClicks(): Observable<Unit> = sendCodeRelay

    override fun confirmClicks(): Observable<Unit> = confirmRelay

}
