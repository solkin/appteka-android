package com.tomclaw.appsend.screen.chat.adapter.incoming

import android.view.View
import android.widget.TextView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.google.android.material.card.MaterialCardView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.formatQuote
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface IncomingMsgItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setTime(time: String)

    fun setDate(date: String?)

    fun setText(text: String)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class IncomingMsgItemViewHolder(view: View) : BaseItemViewHolder(view), IncomingMsgItemView {

    private val dateView: TextView = view.findViewById(R.id.message_date)
    private val memberIconContainer: View = view.findViewById(R.id.member_icon)
    private val userIconView: UserIconView = UserIconViewImpl(memberIconContainer)
    private val bubbleBack: MaterialCardView = view.findViewById(R.id.inc_bubble_back)
    private val textView: TextView = view.findViewById(R.id.inc_text)
    private val timeView: TextView = view.findViewById(R.id.inc_time)

    private var clickListener: (() -> Unit)? = null

    init {
        bubbleBack.setOnClickListener { clickListener?.invoke() }
        memberIconContainer.setOnClickListener { clickListener?.invoke() }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        userIconView.bind(userIcon)
    }

    override fun setTime(time: String) {
        timeView.bind(time)
    }

    override fun setDate(date: String?) {
        dateView.bind(date)
    }

    override fun setText(text: String) {
        textView.text = text.formatQuote()
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
