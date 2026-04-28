package com.tomclaw.appsend.view

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.BadgeMark
import com.tomclaw.appsend.dto.UserIcon


interface UserIconView {

    fun bind(userIcon: UserIcon)

    /**
     * Show or hide the small badge overlay attached to the bottom
     * right corner of the avatar. The host layout must include a
     * `badge_ring` overlay (see `user_icon_s/m/xl.xml`); for
     * layouts without one (e.g. `user_icon_xs.xml`) calls are
     * silently ignored.
     */
    fun bindBadge(badge: BadgeMark?)

}

class UserIconViewImpl(view: View) : UserIconView {

    private val backView: View = view.findViewById(R.id.icon_back)
    private val svgView: SVGImageView = view.findViewById(R.id.icon_svg)

    private val badgeRing: View? = view.findViewById(R.id.badge_ring)
    private val badgeBack: View? = view.findViewById(R.id.badge_back)
    private val badgeSvg: SVGImageView? = view.findViewById(R.id.badge_svg)

    override fun bind(userIcon: UserIcon) {
        var drawable: Drawable = backView.background
        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable, Color.parseColor(userIcon.color))
        backView.background = drawable

        val svg = SVG.getFromString(userIcon.icon)
        svgView.setSVG(svg)

        // Reset the overlay on every bind so recycled item views
        // don't leak a previous user's badge. Callers re-apply via
        // bindBadge() when they have a non-null mark.
        bindBadge(null)
    }

    override fun bindBadge(badge: BadgeMark?) {
        val ring = badgeRing ?: return
        val back = badgeBack ?: return
        val svg = badgeSvg ?: return

        if (badge == null) {
            ring.visibility = View.GONE
            return
        }

        ring.visibility = View.VISIBLE

        val tint = parseColorOrNull(badge.color) ?: Color.GRAY
        val bg = DrawableCompat.wrap(back.background)
        DrawableCompat.setTint(bg, tint)
        back.background = bg

        svg.setSVG(SVG.getFromString(badge.icon))
    }

    private fun parseColorOrNull(color: String?): Int? = try {
        color?.let { Color.parseColor(it) }
    } catch (_: IllegalArgumentException) {
        null
    }

}
