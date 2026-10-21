package com.tomclaw.appsend.screen.upload.adapter.origin

import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.Origin
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface OriginItemView : ItemView {

    fun setOrigin(value: String?)

    fun setOptionEnabled(origin: String, enabled: Boolean)

    fun showOwnDenied(text: String)

    fun hideOwnDenied()

    fun showOwnNote()

    fun hideOwnNote()

    fun showRequiredFieldError()

    fun hideRequiredFieldError()

    fun setOnOriginChangedListener(listener: ((String) -> Unit)?)

}

class OriginItemViewHolder(view: View) : BaseItemViewHolder(view), OriginItemView {

    private val options = mapOf(
        Origin.OWN to Option(
            view,
            R.id.origin_own,
            R.id.origin_own_radio,
            R.id.origin_own_title,
            R.id.origin_own_description
        ),
        Origin.MIRROR to Option(
            view,
            R.id.origin_mirror,
            R.id.origin_mirror_radio,
            R.id.origin_mirror_title,
            R.id.origin_mirror_description
        ),
        Origin.MOD to Option(
            view,
            R.id.origin_mod,
            R.id.origin_mod_radio,
            R.id.origin_mod_title,
            R.id.origin_mod_description
        ),
    )
    private val ownDeniedView: TextView = view.findViewById(R.id.origin_own_denied)
    private val ownNoteView: View = view.findViewById(R.id.origin_own_note)
    private val requiredFieldErrorView: View = view.findViewById(R.id.required_field_error)

    private var originChangedListener: ((String) -> Unit)? = null

    init {
        options.forEach { (origin, option) ->
            option.row.setOnClickListener { originChangedListener?.invoke(origin) }
        }
    }

    override fun setOrigin(value: String?) {
        options.forEach { (origin, option) -> option.radio.isChecked = origin == value }
    }

    override fun setOptionEnabled(origin: String, enabled: Boolean) {
        val option = options[origin] ?: return
        option.row.isEnabled = enabled
        option.radio.isEnabled = enabled
        // Only the option itself fades: the reason it is off must stay readable.
        val alpha = if (enabled) 1f else DISABLED_ALPHA
        option.title.alpha = alpha
        option.description.alpha = alpha
    }

    override fun showOwnDenied(text: String) {
        ownDeniedView.bind(text)
    }

    override fun hideOwnDenied() {
        ownDeniedView.hide()
    }

    override fun showOwnNote() {
        ownNoteView.show()
    }

    override fun hideOwnNote() {
        ownNoteView.hide()
    }

    override fun showRequiredFieldError() {
        requiredFieldErrorView.show()
    }

    override fun hideRequiredFieldError() {
        requiredFieldErrorView.hide()
    }

    override fun setOnOriginChangedListener(listener: ((String) -> Unit)?) {
        this.originChangedListener = listener
    }

    override fun onUnbind() {
        this.originChangedListener = null
    }

    private class Option(view: View, rowId: Int, radioId: Int, titleId: Int, descriptionId: Int) {
        val row: View = view.findViewById(rowId)
        val radio: RadioButton = view.findViewById(radioId)
        val title: View = view.findViewById(titleId)
        val description: View = view.findViewById(descriptionId)
    }

}

private const val DISABLED_ALPHA = 0.38f
