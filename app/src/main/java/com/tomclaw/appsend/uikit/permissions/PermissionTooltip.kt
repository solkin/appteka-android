package com.tomclaw.appsend.uikit.permissions

import android.view.View
import androidx.appcompat.widget.TooltipCompat
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver

/**
 * Attach a tooltip to a disabled control explaining why it is disabled.
 * Uses AppCompat TooltipCompat, which in Material 3 themes renders as
 * the expressive rich tooltip on supported devices.
 *
 * Passing `null` or an allowed [Capability] clears the tooltip, which
 * lets callers use the same entry point for both enabled and disabled
 * states without branching.
 */
object PermissionTooltip {

    fun attach(view: View, capability: Capability?) {
        if (capability == null || capability.allowed) {
            TooltipCompat.setTooltipText(view, null)
            return
        }
        val resolver = CapabilityHintResolver(view.resources)
        TooltipCompat.setTooltipText(view, resolver.resolveText(capability))
    }
}
