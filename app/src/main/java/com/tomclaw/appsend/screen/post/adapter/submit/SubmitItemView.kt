package com.tomclaw.appsend.screen.post.adapter.submit

import android.view.View
import android.widget.Button
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R

interface SubmitItemView : ItemView {

    fun setOnClickListener(listener: (() -> Unit)?)

}

class SubmitItemViewHolder(view: View) : BaseItemViewHolder(view), SubmitItemView {

    private val submitButton: Button = view.findViewById(R.id.submit_button)

    private var clickListener: (() -> Unit)? = null

    init {
        submitButton.setOnClickListener { clickListener?.invoke() }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
