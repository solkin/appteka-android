package com.tomclaw.appsend.screen.upload.adapter.exclusive

import android.view.View
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.google.android.material.materialswitch.MaterialSwitch
import com.tomclaw.appsend.R

interface ExclusiveItemView : ItemView {

    fun setExclusive(value: Boolean)

    fun setOnExclusiveChangedListener(listener: ((Boolean) -> Unit)?)

}

class ExclusiveItemViewHolder(view: View) : BaseItemViewHolder(view), ExclusiveItemView {

    private val exclusiveSwitch: MaterialSwitch = view.findViewById(R.id.exclusive)

    private var exclusiveChangedListener: ((Boolean) -> Unit)? = null

    init {
        exclusiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            exclusiveChangedListener?.invoke(isChecked)
        }
    }

    override fun setExclusive(value: Boolean) {
        exclusiveSwitch.isChecked = value
    }

    override fun setOnExclusiveChangedListener(listener: ((Boolean) -> Unit)?) {
        this.exclusiveChangedListener = listener
    }

    override fun onUnbind() {
        this.exclusiveChangedListener = null
    }

}
