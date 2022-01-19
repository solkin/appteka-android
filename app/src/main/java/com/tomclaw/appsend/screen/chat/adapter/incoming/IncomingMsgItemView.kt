package com.tomclaw.appsend.screen.chat.adapter.incoming

import android.graphics.Color
import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.BubbleColorDrawable
import com.tomclaw.appsend.util.ColorHelper.getAttributedColor
import com.tomclaw.appsend.util.Corner
import com.tomclaw.appsend.util.StringUtil.formatQuote
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface IncomingMsgItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setTime(time: String)

    fun setDate(date: String?)

    fun setText(text: String)

    fun setOnClickListener(listener: (() -> Unit)?)

}

class IncomingMsgItemViewHolder(view: View) : BaseViewHolder(view), IncomingMsgItemView {

    private val context = view.context
    private val dateView: TextView = view.findViewById(R.id.message_date)
    private val userIconView: UserIconView = UserIconViewImpl(view.findViewById(R.id.member_icon))
    private val bubbleBack: View = view.findViewById(R.id.inc_bubble_back)
    private val textView: TextView = view.findViewById(R.id.inc_text)
    private val timeView: TextView = view.findViewById(R.id.inc_time)

    private var clickListener: (() -> Unit)? = null

    init {
        val bubbleColor = getAttributedColor(context, R.attr.discuss_bubble_color)
        bubbleBack.background = BubbleColorDrawable(context, bubbleColor, Corner.LEFT)

        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun setUserIcon(userIcon: UserIcon) {
        userIconView.bind(userIcon)
        textView.setTextColor(Color.parseColor(userIcon.color))
    }

    override fun setTime(time: String) {
        timeView.bind(time)
    }

    override fun setDate(date: String?) {
        dateView.bind(date)
    }

    override fun setText(text: String) {
        textView.text = formatQuote(text)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
