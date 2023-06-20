package com.tomclaw.appsend.screen.upload.adapter.open_source

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface OpenSourceItemView : ItemView {

    fun setOpenSource(value: Boolean)

    fun setUrl(uri: String)

    fun setUrlVisible(visible: Boolean)

    fun setOnOpenSourceChangedListener(listener: ((Boolean, String) -> Unit)?)

}

class OpenSourceItemViewHolder(view: View) : BaseViewHolder(view), OpenSourceItemView {

    private val openSourceCheckBox: CheckBox = view.findViewById(R.id.open_source)
    private val sourceUrlEdit: EditText = view.findViewById(R.id.source_url)

    private var openSourceChangedListener: ((Boolean, String) -> Unit)? = null

    init {
        openSourceCheckBox.setOnCheckedChangeListener { _, isChecked ->
            openSourceChangedListener?.invoke(isChecked, sourceUrlEdit.text.toString())
        }
        sourceUrlEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                openSourceChangedListener?.invoke(openSourceCheckBox.isChecked, s.toString())
            }
        })
    }

    override fun setOpenSource(value: Boolean) {
        openSourceCheckBox.isChecked = value
    }

    override fun setUrl(uri: String) {
        sourceUrlEdit.setText(uri)
    }

    override fun setUrlVisible(visible: Boolean) {
        sourceUrlEdit.isVisible = visible
    }

    override fun setOnOpenSourceChangedListener(listener: ((Boolean, String) -> Unit)?) {
        this.openSourceChangedListener = listener
    }

    override fun onUnbind() {
        this.openSourceChangedListener = null
    }

}
