package com.tomclaw.appsend.screen.agreement

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable

interface AgreementView {

    fun setAgreed(check: Boolean)

    fun enableSubmitButton()

    fun disableSubmitButton()

    fun navigationClicks(): Observable<Unit>

    fun agreementClicks(): Observable<Boolean>

    fun submitClicks(): Observable<Unit>

}

class AgreementViewImpl(view: View) : AgreementView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val agreementCheck: CheckBox = view.findViewById(R.id.agreement_check)
    private val submitButton: Button = view.findViewById(R.id.submit_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val agreementRelay = PublishRelay.create<Boolean>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.upload_notice_title)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        agreementCheck.setOnCheckedChangeListener { _, isChecked ->
            agreementRelay.accept(isChecked)
        }
        submitButton.setOnClickListener { submitRelay.accept(Unit) }
    }

    override fun setAgreed(check: Boolean) {
        agreementCheck.isChecked = check
    }

    override fun enableSubmitButton() {
        submitButton.isEnabled = true
    }

    override fun disableSubmitButton() {
        submitButton.isEnabled = false
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun agreementClicks(): Observable<Boolean> = agreementRelay

    override fun submitClicks(): Observable<Unit> = submitRelay

}
