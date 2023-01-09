package com.tomclaw.appsend.screen.details.adapter.discuss

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show

interface DiscussItemView : ItemView {

    fun showMsgCount(value: Int)

    fun showNoMsgIndicator()

    fun setOnDiscussClickListener(listener: (() -> Unit)?)

}

class DiscussItemViewHolder(view: View) : BaseViewHolder(view), DiscussItemView {

    private val msgCountView: TextView = view.findViewById(R.id.msg_count)
    private val noMsgIndicatorView: View = view.findViewById(R.id.no_msg_indicator)

    private var discussClickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { discussClickListener?.invoke() }
    }

    override fun showMsgCount(value: Int) {
        msgCountView.bind(value.toString())
        noMsgIndicatorView.hide()
    }

    override fun showNoMsgIndicator() {
        msgCountView.hide()
        noMsgIndicatorView.show()
    }

    override fun setOnDiscussClickListener(listener: (() -> Unit)?) {
        this.discussClickListener = listener
    }

    override fun onUnbind() {
        this.discussClickListener = null
    }

}
