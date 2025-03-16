package com.tomclaw.appsend.screen.post.adapter.submit

import android.view.View
import android.widget.Button
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.disable
import com.tomclaw.appsend.util.enable

interface SubmitItemView : ItemView {

    fun setEnabled()

    fun setDisabled()

    fun setOnClickListener(listener: (() -> Unit)?)

}

class SubmitItemViewHolder(view: View) : BaseViewHolder(view), SubmitItemView {

    private val submitButton: Button = view.findViewById(R.id.submit_button)

    private var clickListener: (() -> Unit)? = null

    init {
        submitButton.setOnClickListener { clickListener?.invoke() }
    }

    override fun setEnabled() {
        submitButton.enable()
    }

    override fun setDisabled() {
        submitButton.disable()
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
