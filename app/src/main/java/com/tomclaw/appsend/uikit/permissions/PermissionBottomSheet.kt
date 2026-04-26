package com.tomclaw.appsend.uikit.permissions

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityHintResolver

/**
 * Modal bottom sheet that explains an ACL restriction in detail.
 * Opens only for denied capabilities — no-op otherwise, so callers can
 * pass an optional capability without guarding every call site.
 */
object PermissionBottomSheet {

    fun show(
        context: Context,
        capability: Capability?,
        title: CharSequence = context.getString(R.string.permission_banner_read_only_title),
    ) {
        if (capability == null || capability.allowed) return

        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_permission, null, false)

        view.findViewById<TextView>(R.id.permission_sheet_title).text = title
        view.findViewById<TextView>(R.id.permission_sheet_body).text =
            CapabilityHintResolver(context.resources).resolveText(capability)
        view.findViewById<MaterialButton>(R.id.permission_sheet_dismiss).setOnClickListener {
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }
}
