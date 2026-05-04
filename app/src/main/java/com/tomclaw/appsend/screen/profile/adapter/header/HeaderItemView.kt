package com.tomclaw.appsend.screen.profile.adapter.header

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.Badge
import com.tomclaw.appsend.dto.BadgeMark
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import com.tomclaw.appsend.util.svgToDrawable
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface HeaderItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserBadge(badge: BadgeMark?)

    fun setBadges(badges: List<Badge>?)

    fun setOnBadgeClickListener(listener: ((Badge) -> Unit)?)

    fun setUserName(name: String)

    fun setUserEmail(email: String?)

    /**
     * Render the freeform "about you" line. Hides the row when the
     * bio is null/blank — the field stays optional so old or
     * unprivileged users keep the same compact header as before.
     */
    fun setUserBio(bio: String?)

    /**
     * Renders the user's metadata line. When [onlineHighlight] is
     * non-null, the first occurrence of that substring inside [value]
     * is painted in the "online" accent colour — used to mark active
     * users without introducing a separate UI element next to the
     * avatar (which is now reserved for the primary badge overlay).
     */
    fun setUserDescription(value: String, onlineHighlight: String?)

    fun showSubscribeButton()

    fun hideSubscribeButton()

    fun showUnsubscribeButton()

    fun hideUnsubscribeButton()

    fun showUserNameEditIcon()

    fun showUserEmailEditIcon()

    fun setOnNameClickListener(listener: (() -> Unit)?)

    fun setOnAvatarClickListener(listener: (() -> Unit)?)

    fun setOnEmailClickListener(listener: (() -> Unit)?)

    fun setOnSubscribeClickListener(listener: (() -> Unit)?)

    fun setOnUnsubscribeClickListener(listener: (() -> Unit)?)

}

class HeaderItemViewHolder(private val view: View) : BaseItemViewHolder(view), HeaderItemView {

    private val resources = view.resources
    private val userIconContainer: View = view.findViewById(R.id.user_icon)
    private val userIcon: UserIconView = UserIconViewImpl(userIconContainer)
    private val userName: TextView = view.findViewById(R.id.user_name)
    private val userEmail: TextView = view.findViewById(R.id.user_email)
    private val userBioContainer: View = view.findViewById(R.id.user_bio_container)
    private val userBio: TextView = view.findViewById(R.id.user_bio)
    private val userDescription: TextView = view.findViewById(R.id.user_description)
    private val subscribeButton: View = view.findViewById(R.id.subscribe_button)
    private val unsubscribeButton: View = view.findViewById(R.id.unsubscribe_button)
    private val badgesGroup: ChipGroup = view.findViewById(R.id.user_badges_group)

    private var nameClickListener: (() -> Unit)? = null
    private var avatarClickListener: (() -> Unit)? = null
    private var emailClickListener: (() -> Unit)? = null
    private var subscribeClickListener: (() -> Unit)? = null
    private var unsubscribeClickListener: (() -> Unit)? = null
    private var badgeClickListener: ((Badge) -> Unit)? = null
    private var boundBadges: List<Badge> = emptyList()

    override fun setUserIcon(userIcon: UserIcon) {
        this.userIcon.bind(userIcon)
    }

    override fun setUserBadge(badge: BadgeMark?) {
        this.userIcon.bindBadge(badge)
    }

    override fun setBadges(badges: List<Badge>?) {
        boundBadges = badges.orEmpty()
        badgesGroup.removeAllViews()
        if (boundBadges.isEmpty()) {
            badgesGroup.isVisible = false
            return
        }
        badgesGroup.isVisible = true
        boundBadges.forEachIndexed { index, badge ->
            val chip = Chip(badgesGroup.context).apply {
                id = View.generateViewId()
                text = badge.name
                isClickable = true
                isCheckable = false
                isChipIconVisible = true
                // Disable the 48dp accessibility-minimum padding around
                // the chip so multi-row layouts don't get a giant
                // vertical gap between rows. The chip is still easy to
                // tap because its content already has comfortable
                // padding from the Material defaults.
                setEnsureMinTouchTargetSize(false)
                val tint = parseColorOrNull(badge.color) ?: Color.GRAY
                chipIconTint = ColorStateList.valueOf(tint)
                try {
                    chipIcon = svgToDrawable(badge.icon, resources)
                } catch (_: Throwable) {
                    chipIcon = null
                }
                setOnClickListener {
                    badgeClickListener?.invoke(boundBadges.getOrNull(index) ?: return@setOnClickListener)
                }
            }
            badgesGroup.addView(chip)
        }
    }

    override fun setOnBadgeClickListener(listener: ((Badge) -> Unit)?) {
        this.badgeClickListener = listener
    }

    private fun parseColorOrNull(color: String?): Int? = try {
        color?.let { Color.parseColor(it) }
    } catch (_: IllegalArgumentException) {
        null
    }

    override fun setUserDescription(value: String, onlineHighlight: String?) {
        if (onlineHighlight.isNullOrEmpty()) {
            userDescription.text = value
            return
        }
        val start = value.indexOf(onlineHighlight)
        if (start < 0) {
            userDescription.text = value
            return
        }
        val span = SpannableString(value)
        val color = ContextCompat.getColor(view.context, R.color.online_color)
        span.setSpan(
            ForegroundColorSpan(color),
            start,
            start + onlineHighlight.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        userDescription.text = span
    }

    override fun setUserName(name: String) {
        userName.bind(name)
    }

    override fun setUserEmail(email: String?) {
        if (!email.isNullOrBlank()) {
            userEmail.text = email
            userEmail.show()
        } else {
            userEmail.hide()
        }
    }

    override fun setUserBio(bio: String?) {
        if (!bio.isNullOrBlank()) {
            userBio.text = bio
            userBioContainer.show()
        } else {
            userBioContainer.hide()
        }
    }

    override fun showSubscribeButton() {
        subscribeButton.show()
    }

    override fun hideSubscribeButton() {
        subscribeButton.hide()
    }

    override fun showUnsubscribeButton() {
        unsubscribeButton.show()
    }

    override fun hideUnsubscribeButton() {
        unsubscribeButton.hide()
    }

    override fun showUserNameEditIcon() {
        userName.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_edit, null),
            null
        )
    }

    override fun showUserEmailEditIcon() {
        userEmail.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            ResourcesCompat.getDrawable(resources, R.drawable.ic_email_edit_small, null),
            null
        )
    }

    override fun setOnNameClickListener(listener: (() -> Unit)?) {
        this.nameClickListener = listener

        userName.setOnClickListener(listener?.let { { nameClickListener?.invoke() } })
    }

    override fun setOnAvatarClickListener(listener: (() -> Unit)?) {
        this.avatarClickListener = listener

        if (listener != null) {
            userIconContainer.isClickable = true
            userIconContainer.foreground =
                ContextCompat.getDrawable(view.context, R.drawable.circle_ripple)
            userIconContainer.setOnClickListener { avatarClickListener?.invoke() }
        } else {
            userIconContainer.setOnClickListener(null)
            userIconContainer.foreground = null
            userIconContainer.isClickable = false
        }
    }

    override fun setOnEmailClickListener(listener: (() -> Unit)?) {
        this.emailClickListener = listener

        userEmail.setOnClickListener(listener?.let { { emailClickListener?.invoke() } })
    }

    override fun setOnSubscribeClickListener(listener: (() -> Unit)?) {
        subscribeClickListener = listener

        subscribeButton.setOnClickListener(listener?.let { { subscribeClickListener?.invoke() } })
    }

    override fun setOnUnsubscribeClickListener(listener: (() -> Unit)?) {
        unsubscribeClickListener = listener

        unsubscribeButton.setOnClickListener(listener?.let { { unsubscribeClickListener?.invoke() } })
    }

    override fun onUnbind() {
        this.nameClickListener = null
        this.avatarClickListener = null
        this.emailClickListener = null
        this.subscribeClickListener = null
        this.unsubscribeClickListener = null
        this.badgeClickListener = null
    }

}
