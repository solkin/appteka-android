package com.tomclaw.appsend.screen.upload.adapter.origin

import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.color.MaterialColors
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.Origin

interface OriginItemView : ItemView {

    fun setOrigin(value: String?)

    fun setOwnEnabled(enabled: Boolean)

    fun showDescription(origin: String?)

    fun showOwnDenied(text: String)

    fun showRequiredFieldError()

    fun setOnOriginClickListener(listener: ((String) -> Unit)?)

}

class OriginItemViewHolder(view: View) : BaseItemViewHolder(view), OriginItemView {

    private val group: MaterialButtonToggleGroup = view.findViewById(R.id.origin_group)
    private val buttons: Map<String, MaterialButton> = mapOf(
        Origin.OWN to view.findViewById(R.id.origin_own),
        Origin.MIRROR to view.findViewById(R.id.origin_mirror),
        Origin.MOD to view.findViewById(R.id.origin_mod),
    )
    private val hint: TextView = view.findViewById(R.id.origin_hint)

    private var originClickListener: ((String) -> Unit)? = null

    init {
        // Clicks, not the checked listener: re-checking on every bind must
        // not read as the uploader picking something.
        buttons.forEach { (origin, button) ->
            button.setOnClickListener { originClickListener?.invoke(origin) }
        }
    }

    override fun setOrigin(value: String?) {
        val button = buttons[value]
        if (button != null) group.check(button.id) else group.clearChecked()
    }

    override fun setOwnEnabled(enabled: Boolean) {
        buttons[Origin.OWN]?.isEnabled = enabled
    }

    override fun showDescription(origin: String?) {
        val text = when (origin) {
            Origin.OWN -> R.string.origin_own_description
            Origin.MIRROR -> R.string.origin_mirror_description
            Origin.MOD -> R.string.origin_mod_description
            else -> R.string.origin_hint
        }
        showHint(hint.context.getString(text), isError = false)
    }

    override fun showOwnDenied(text: String) {
        showHint(text, isError = false)
    }

    override fun showRequiredFieldError() {
        showHint(hint.context.getString(R.string.required_field), isError = true)
    }

    override fun setOnOriginClickListener(listener: ((String) -> Unit)?) {
        this.originClickListener = listener
    }

    override fun onUnbind() {
        this.originClickListener = null
    }

    private fun showHint(text: String, isError: Boolean) {
        hint.text = text
        val color = if (isError) {
            androidx.appcompat.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurfaceVariant
        }
        hint.setTextColor(MaterialColors.getColor(hint, color))
    }

}
