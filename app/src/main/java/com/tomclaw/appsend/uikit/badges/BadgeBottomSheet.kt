package com.tomclaw.appsend.uikit.badges

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.Badge

/**
 * Modal bottom sheet showing a single profile [Badge] in detail —
 * coloured disc with the badge icon, headline and description. Mirrors
 * the visual language of [BadgeMark] avatar overlays so the user can
 * recognise which chip they tapped.
 */
object BadgeBottomSheet {

    fun show(context: Context, badge: Badge) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_badge, null, false)

        val disc: View = view.findViewById(R.id.badge_disc)
        val icon: SVGImageView = view.findViewById(R.id.badge_icon)
        val title: TextView = view.findViewById(R.id.badge_title)
        val description: TextView = view.findViewById(R.id.badge_description)
        val dismiss: MaterialButton = view.findViewById(R.id.badge_dismiss)

        val tint = parseColorOrNull(badge.color) ?: Color.GRAY
        val bg: Drawable = DrawableCompat.wrap(disc.background)
        DrawableCompat.setTint(bg, tint)
        disc.background = bg

        icon.setSVG(SVG.getFromString(badge.icon))

        title.text = badge.name
        description.text = badge.description.orEmpty()
        description.isVisible = !badge.description.isNullOrBlank()

        dismiss.setOnClickListener { dialog.dismiss() }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun parseColorOrNull(color: String?): Int? = try {
        color?.let { Color.parseColor(it) }
    } catch (_: IllegalArgumentException) {
        null
    }
}
