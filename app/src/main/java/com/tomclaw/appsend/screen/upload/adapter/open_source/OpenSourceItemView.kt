package com.tomclaw.appsend.screen.upload.adapter.open_source

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputLayout
import com.tomclaw.appsend.R

interface OpenSourceItemView : ItemView {

    fun setOpenSource(value: Boolean)

    fun setUrl(uri: String)

    fun setUrlVisible(visible: Boolean)

    fun setOnOpenSourceChangedListener(listener: ((Boolean, String) -> Unit)?)

}

class OpenSourceItemViewHolder(view: View) : BaseItemViewHolder(view), OpenSourceItemView {

    private val openSourceSwitch: MaterialSwitch = view.findViewById(R.id.open_source)
    private val sourceUrlLayout: TextInputLayout = view.findViewById(R.id.source_url_layout)
    private val sourceUrlEdit: EditText = view.findViewById(R.id.source_url)

    private var openSourceChangedListener: ((Boolean, String) -> Unit)? = null

    init {
        openSourceSwitch.setOnCheckedChangeListener { _, isChecked ->
            openSourceChangedListener?.invoke(isChecked, sourceUrlEdit.text.toString())
        }
        sourceUrlEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                openSourceChangedListener?.invoke(openSourceSwitch.isChecked, s.toString())
            }
        })
    }

    override fun setOpenSource(value: Boolean) {
        openSourceSwitch.isChecked = value
    }

    override fun setUrl(uri: String) {
        sourceUrlEdit.setText(uri)
    }

    override fun setUrlVisible(visible: Boolean) {
        sourceUrlLayout.isVisible = visible
    }

    override fun setOnOpenSourceChangedListener(listener: ((Boolean, String) -> Unit)?) {
        this.openSourceChangedListener = listener
    }

    override fun onUnbind() {
        this.openSourceChangedListener = null
    }

}
