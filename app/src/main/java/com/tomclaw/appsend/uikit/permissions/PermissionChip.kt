package com.tomclaw.appsend.uikit.permissions

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver

/**
 * Compact Material 3 assist-chip that surfaces an ACL restriction next to
 * a specific control (e.g. near a message composer). Meant for cases
 * where a full-width banner would be too heavy.
 */
class PermissionChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipStyle,
) : Chip(context, attrs, defStyleAttr) {

    private val hintResolver = CapabilityHintResolver(resources)

    init {
        chipIcon = context.getDrawable(R.drawable.ic_lock)
        isChipIconVisible = true
        isClickable = false
        isFocusable = false
        isVisible = false
    }

    fun showFor(capability: Capability?) {
        if (capability == null || capability.allowed) {
            isVisible = false
            return
        }
        text = hintResolver.resolveText(capability)
        isVisible = true
    }

    fun hide() {
        isVisible = false
    }
}
