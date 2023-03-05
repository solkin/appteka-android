package com.tomclaw.appsend.screen.upload.adapter.exclusive

import android.view.View
import android.widget.CheckBox
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface ExclusiveItemView : ItemView {

    fun setExclusive(value: Boolean)

    fun setOnExclusiveChangedListener(listener: ((Boolean) -> Unit)?)

}

@Suppress("DEPRECATION")
class ExclusiveItemViewHolder(view: View) : BaseViewHolder(view), ExclusiveItemView {

    private val exclusiveCheckBox: CheckBox = view.findViewById(R.id.exclusive)

    private var exclusiveChangedListener: ((Boolean) -> Unit)? = null

    init {
        exclusiveCheckBox.setOnCheckedChangeListener { _, isChecked ->
            exclusiveChangedListener?.invoke(isChecked)
        }
    }

    override fun setExclusive(value: Boolean) {
        exclusiveCheckBox.isChecked = value
    }

    override fun setOnExclusiveChangedListener(listener: ((Boolean) -> Unit)?) {
        this.exclusiveChangedListener = listener
    }

    override fun onUnbind() {
        this.exclusiveChangedListener = null
    }

}
