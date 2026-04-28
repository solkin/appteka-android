package com.tomclaw.appsend.view

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGImageView
import com.google.android.material.imageview.ShapeableImageView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.BadgeMark
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.imageloader.util.fetch


interface UserIconView {

    fun bind(userIcon: UserIcon)

    /**
     * Overlay an arbitrary local image on top of the procedural SVG —
     * used when an editor surfaces an unsaved (e.g. just-cropped) pick
     * via a `file://` or `content://` URI. Silently a no-op for layouts
     * without an `icon_image` overlay.
     */
    fun bindLocalImage(uri: Uri)

    /**
     * Drop any locally-overlaid image so the procedural SVG bound by
     * [bind] is visible again. Pairs with [bindLocalImage].
     */
    fun clearLocalImage()

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
    private val imageView: ShapeableImageView? = view.findViewById(R.id.icon_image)

    private val badgeRing: View? = view.findViewById(R.id.badge_ring)
    private val badgeBack: View? = view.findViewById(R.id.badge_back)
    private val badgeSvg: SVGImageView? = view.findViewById(R.id.badge_svg)

    override fun bind(userIcon: UserIcon) {
        var drawable: Drawable = backView.background
        drawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(drawable, Color.parseColor(userIcon.color))
        backView.background = drawable

        // The procedural SVG glyph is bound unconditionally — it
        // serves as a fallback while the uploaded image (if any)
        // is still being fetched, and the only visible icon for
        // users without an upload.
        val svg = SVG.getFromString(userIcon.icon)
        svgView.setSVG(svg)

        // Image overlay: when the user has an uploaded picture and
        // the host layout exposes the icon_image slot, swap the
        // SVG out and load the preview URL through the project's
        // image loader (centerCrop, fits the circular shape).
        val image = userIcon.image
        if (image != null) {
            showImageOverlay(image.previewUrl)
        } else {
            hideImageOverlay()
        }

        // Reset the overlay on every bind so recycled item views
        // don't leak a previous user's badge. Callers re-apply via
        // bindBadge() when they have a non-null mark.
        bindBadge(null)
    }

    override fun bindLocalImage(uri: Uri) {
        showImageOverlay(uri.toString())
    }

    override fun clearLocalImage() {
        hideImageOverlay()
    }

    private fun showImageOverlay(source: String) {
        val target = imageView ?: return
        target.visibility = View.VISIBLE
        target.fetch(source) {
            centerCrop()
        }
    }

    private fun hideImageOverlay() {
        val target = imageView ?: return
        target.setImageDrawable(null)
        target.visibility = View.GONE
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
