package com.tomclaw.appsend.screen.upload.adapter.submit

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface SubmitItemView : ItemView {

    fun setOnClickListener(listener: (() -> Unit)?)

}

@Suppress("DEPRECATION")
class SubmitItemViewHolder(view: View) : BaseViewHolder(view), SubmitItemView {

    private val uploadButton: View = view.findViewById(R.id.upload_button)

    private var clickListener: (() -> Unit)? = null

    init {
        uploadButton.setOnClickListener { clickListener?.invoke() }
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
