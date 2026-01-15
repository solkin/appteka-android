package com.tomclaw.appsend.screen.details.adapter.abi

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface AbiItemView : ItemView {

    fun showArchitectures(architectures: List<String>)

    fun showCompatibility(text: String, isCompatible: Boolean)

}

class AbiItemViewHolder(view: View) : BaseViewHolder(view), AbiItemView {

    private val architecturesText: TextView = view.findViewById(R.id.architectures_text)
    private val compatibilityText: TextView = view.findViewById(R.id.compatibility_text)
    private val compatibilityIcon: ImageView = view.findViewById(R.id.compatibility_icon)

    override fun showArchitectures(architectures: List<String>) {
        architecturesText.text = architectures.joinToString(", ")
    }

    override fun showCompatibility(text: String, isCompatible: Boolean) {
        compatibilityText.text = text

        val colorRes = if (isCompatible) {
            R.color.abi_compatible_color
        } else {
            R.color.abi_incompatible_color
        }
        val color = ContextCompat.getColor(itemView.context, colorRes)
        compatibilityText.setTextColor(color)

        val iconRes = if (isCompatible) {
            R.drawable.ic_verified
        } else {
            R.drawable.ic_alert_circle
        }
        compatibilityIcon.setImageResource(iconRes)
        compatibilityIcon.imageTintList = ContextCompat.getColorStateList(itemView.context, colorRes)
    }

    override fun onUnbind() {
        // No-op
    }

}
