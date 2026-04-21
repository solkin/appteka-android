package com.tomclaw.appsend.screen.chat.adapter.system

import android.view.View
import android.widget.TextView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.util.bind

interface SystemMsgItemView : ItemView {

    fun setText(text: String)

    fun setDate(date: String?)

}

class SystemMsgItemViewHolder(view: View) : BaseItemViewHolder(view), SystemMsgItemView {

    private val dateView: TextView = view.findViewById(R.id.message_date)
    private val textView: TextView = view.findViewById(R.id.system_text)

    override fun setText(text: String) {
        textView.text = text
    }

    override fun setDate(date: String?) {
        dateView.bind(date)
    }

    override fun onUnbind() {
        // no-op
    }

}
