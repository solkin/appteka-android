package com.tomclaw.appsend.screen.chat.adapter.incoming

import android.content.res.Configuration
import android.graphics.Color
import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.google.android.material.card.MaterialCardView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.BadgeMark
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.view.MessageAttachmentsView
import com.tomclaw.appsend.util.LinkMovementMethodCompat
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.formatMessageText
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface IncomingMsgItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setUserBadge(badge: BadgeMark?)

    fun setAuthor(name: String?, color: String?)

    fun setTime(time: String)

    fun setDate(date: String?)

    fun setText(text: String)

    fun setAttachments(attachments: List<MsgAttachment>?)

    fun setOnClickListener(listener: (() -> Unit)?)

    fun setOnAvatarClickListener(listener: (() -> Unit)?)

    fun setOnAttachmentClickListener(listener: ((Int) -> Unit)?)

}

class IncomingMsgItemViewHolder(view: View) : BaseItemViewHolder(view), IncomingMsgItemView {

    private val dateView: TextView = view.findViewById(R.id.message_date)
    private val memberIconContainer: View = view.findViewById(R.id.member_icon)
    private val userIconView: UserIconView = UserIconViewImpl(memberIconContainer)
    private val bubbleBack: MaterialCardView = view.findViewById(R.id.inc_bubble_back)
    private val authorView: TextView = view.findViewById(R.id.inc_author)
    private val attachmentsView: MessageAttachmentsView = view.findViewById(R.id.inc_attachments)
    private val textView: TextView = view.findViewById(R.id.inc_text)
    private val timeView: TextView = view.findViewById(R.id.inc_time)

    private var clickListener: (() -> Unit)? = null
    private var avatarClickListener: (() -> Unit)? = null
    private var attachmentClickListener: ((Int) -> Unit)? = null

    init {
        bubbleBack.setOnClickListener { clickListener?.invoke() }
        memberIconContainer.setOnClickListener { avatarClickListener?.invoke() }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        userIconView.bind(userIcon)
    }

    override fun setUserBadge(badge: BadgeMark?) {
        userIconView.bindBadge(badge)
    }

    override fun setAuthor(name: String?, color: String?) {
        if (name.isNullOrEmpty()) {
            authorView.visibility = View.GONE
            return
        }
        authorView.visibility = View.VISIBLE
        authorView.text = name
        authorView.setTextColor(resolveAuthorColor(color))
    }

    private fun resolveAuthorColor(raw: String?): Int {
        val parsed = raw?.let {
            try {
                Color.parseColor(it)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
        val base = parsed ?: run {
            val typed = TypedValue()
            authorView.context.theme.resolveAttribute(
                androidx.appcompat.R.attr.colorPrimary, typed, true
            )
            typed.data
        }
        val isNight = (authorView.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        return if (isNight) ColorUtils.blendARGB(base, Color.WHITE, 0.35f) else base
    }

    override fun setTime(time: String) {
        timeView.bind(time)
    }

    override fun setDate(date: String?) {
        dateView.bind(date)
    }

    override fun setText(text: String) {
        textView.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        textView.text = formatMessageText(text, textView.context)
        val hasLinks = Linkify.addLinks(
            textView,
            Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
        )
        if (hasLinks) {
            textView.movementMethod = LinkMovementMethodCompat
        } else {
            textView.movementMethod = null
        }
        textView.isFocusable = false
        textView.isClickable = false
        textView.isLongClickable = false
    }

    override fun setAttachments(attachments: List<MsgAttachment>?) {
        if (attachments.isNullOrEmpty()) {
            attachmentsView.visibility = View.GONE
            return
        }
        attachmentsView.visibility = View.VISIBLE
        attachmentsView.setAttachments(attachments) { index ->
            attachmentClickListener?.invoke(index)
        }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun setOnAvatarClickListener(listener: (() -> Unit)?) {
        this.avatarClickListener = listener
    }

    override fun setOnAttachmentClickListener(listener: ((Int) -> Unit)?) {
        this.attachmentClickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
        this.avatarClickListener = null
        this.attachmentClickListener = null
    }

}
