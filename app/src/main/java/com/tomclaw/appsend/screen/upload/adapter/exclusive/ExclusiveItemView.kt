package com.tomclaw.appsend.screen.upload.adapter.exclusive

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.materialswitch.MaterialSwitch
import com.tomclaw.appsend.R

interface ExclusiveItemView : ItemView {

    fun setExclusive(value: Boolean)

    fun setOnExclusiveChangedListener(listener: ((Boolean) -> Unit)?)

}

class ExclusiveItemViewHolder(view: View) : BaseViewHolder(view), ExclusiveItemView {

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
