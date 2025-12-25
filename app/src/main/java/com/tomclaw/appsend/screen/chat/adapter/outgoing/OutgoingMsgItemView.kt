package com.tomclaw.appsend.screen.chat.adapter.outgoing

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat.getDrawable
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.card.MaterialCardView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.formatQuote
import com.tomclaw.appsend.view.UserIconView
import com.tomclaw.appsend.view.UserIconViewImpl

interface OutgoingMsgItemView : ItemView {

    fun setUserIcon(userIcon: UserIcon)

    fun setTime(time: String)

    fun setDate(date: String?)

    fun setText(text: String)

    fun sendingState()

    fun sentState()

    fun deliveredState()

    fun setOnClickListener(listener: (() -> Unit)?)

}

class OutgoingMsgItemViewHolder(view: View) : BaseViewHolder(view), OutgoingMsgItemView {

    private val resources = view.resources
    private val dateView: TextView = view.findViewById(R.id.message_date)
    private val memberIconContainer: View = view.findViewById(R.id.member_icon)
    private val userIconView: UserIconView = UserIconViewImpl(memberIconContainer)
    private val bubbleBack: MaterialCardView = view.findViewById(R.id.out_bubble_back)
    private val delivery: ImageView = view.findViewById(R.id.message_delivery)
    private val textView: TextView = view.findViewById(R.id.out_text)
    private val timeView: TextView = view.findViewById(R.id.out_time)

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

    override fun sendingState() {
        delivery.setImageDrawable(getDrawable(resources, R.drawable.clock, null))
    }

    override fun sentState() {
        delivery.setImageDrawable(getDrawable(resources, R.drawable.check_circle, null))
    }

    override fun deliveredState() {
        delivery.setImageDrawable(getDrawable(resources, R.drawable.check_all, null))
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
