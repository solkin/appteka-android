package com.tomclaw.appsend.screen.restrict

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver
import com.tomclaw.appsend.dto.RejectionReason
import com.tomclaw.appsend.uikit.reasons.ReasonsAdapter
import com.tomclaw.appsend.util.applyBottomInsetsWithImeAsMargin
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.hideWithAlphaAnimation
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.showWithAlphaAnimation
import io.reactivex.rxjava3.core.Observable

interface RestrictView {

    fun showProgress()

    fun showContent()

    fun setDescription(text: String)

    fun setReason(text: String?)

    fun setComment(text: String)

    fun setSubmitLabel(text: String)

    fun setSubmitEnabled(enabled: Boolean)

    fun setWarning(text: String?)

    fun showReasonPicker(reasons: List<RejectionReason>)

    fun showFailed()

    fun showUnauthorizedError()

    /**
     * Surface a capability-denied response (server rejected the
     * action because of an ACL/ownership/role check). Routed through
     * the same hint resolver as proactive UI.
     */
    fun showCapabilityDenied(capability: Capability)

    fun navigationClicks(): Observable<Unit>

    fun reasonClicks(): Observable<Unit>

    fun reasonPicked(): Observable<RejectionReason>

    fun commentChanged(): Observable<String>

    fun submitClicks(): Observable<Unit>

}

class RestrictViewImpl(
    view: View,
    title: String
) : RestrictView {

    private val context = view.context
    private val scrollView: View = view.findViewById(R.id.scroll_view)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val descriptionView: TextView = view.findViewById(R.id.description)
    private val reasonLayout: TextInputLayout = view.findViewById(R.id.reason_input_layout)
    private val reasonView: TextInputEditText = view.findViewById(R.id.reason_input)
    private val commentView: TextInputEditText = view.findViewById(R.id.comment_input)
    private val submitButton: MaterialButton = view.findViewById(R.id.submit_button)
    private val warningView: TextView = view.findViewById(R.id.warning)
    private val overlayProgress: View = view.findViewById(R.id.overlay_progress)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val reasonClickRelay = PublishRelay.create<Unit>()
    private val reasonPickedRelay = PublishRelay.create<RejectionReason>()
    private val commentChangedRelay = PublishRelay.create<String>()
    private val submitRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        toolbar.title = title

        // The reason is picked from a list, never typed: the catalog is
        // what the author gets shown, translated into their language.
        reasonView.setOnClickListener { reasonClickRelay.accept(Unit) }
        reasonLayout.setEndIconOnClickListener { reasonClickRelay.accept(Unit) }

        commentView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                commentChangedRelay.accept(s.toString())
            }
        })
        submitButton.setOnClickListener { submitRelay.accept(Unit) }

        // Insets: content keeps clear of the navigation bar, and of the
        // keyboard so the field being filled in stays in sight.
        scrollView.applyBottomInsetsWithImeAsMargin()
    }

    override fun showProgress() {
        overlayProgress.showWithAlphaAnimation(animateFully = true)
    }

    override fun showContent() {
        overlayProgress.hideWithAlphaAnimation(animateFully = false)
    }

    override fun setDescription(text: String) {
        descriptionView.text = text
    }

    override fun setReason(text: String?) {
        reasonView.setText(text.orEmpty())
    }

    override fun setComment(text: String) {
        commentView.setText(text)
    }

    override fun setSubmitLabel(text: String) {
        submitButton.text = text
    }

    override fun setSubmitEnabled(enabled: Boolean) {
        submitButton.isEnabled = enabled
    }

    override fun setWarning(text: String?) {
        if (text == null) {
            warningView.hide()
        } else {
            warningView.text = text
            warningView.show()
        }
    }

    override fun showReasonPicker(reasons: List<RejectionReason>) {
        val sheet = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_reasons, null)

        val list = sheetView.findViewById<RecyclerView>(R.id.reasons_list)
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = ReasonsAdapter(reasons) { reason ->
            sheet.dismiss()
            reasonPickedRelay.accept(reason)
        }

        sheet.setContentView(sheetView)
        sheet.show()
    }

    override fun showFailed() {
        Snackbar.make(scrollView, R.string.unable_to_unfile, Snackbar.LENGTH_SHORT).show()
    }

    override fun showUnauthorizedError() {
        Snackbar.make(
            scrollView,
            R.string.authorization_required_message,
            Snackbar.LENGTH_LONG
        ).show()
    }

    override fun showCapabilityDenied(capability: Capability) {
        val text = CapabilityHintResolver(scrollView.resources).resolveText(capability)
        Snackbar.make(scrollView, text, Snackbar.LENGTH_LONG).show()
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun reasonClicks(): Observable<Unit> = reasonClickRelay

    override fun reasonPicked(): Observable<RejectionReason> = reasonPickedRelay

    override fun commentChanged(): Observable<String> = commentChangedRelay

    override fun submitClicks(): Observable<Unit> = submitRelay

}
