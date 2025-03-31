package com.tomclaw.appsend.screen.post.adapter.text

import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface TextItemView : ItemView {

    fun setMaxLength(length: Int)

    fun setText(text: String)

    fun showRequiredFieldError()

    fun hideRequiredFieldError()

    fun setOnTextChangedListener(listener: ((String) -> Unit)?)

}

class TextItemViewHolder(view: View) : BaseViewHolder(view), TextItemView {

    private val resources = view.resources
    private val textEdit: EditText = view.findViewById(R.id.text)

    private var textChangedListener: ((String) -> Unit)? = null

    init {
        textEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                textChangedListener?.invoke(s.toString())
            }
        })
    }

    override fun setMaxLength(length: Int) {
        textEdit.setFilters(
            arrayOf<InputFilter>(
                LengthFilter(length)
            )
        )
    }

    override fun setText(text: String) {
        textEdit.setText(text)
    }

    override fun showRequiredFieldError() {
        textEdit.error = resources.getString(R.string.required_field)
    }

    override fun hideRequiredFieldError() {
        textEdit.error = null
    }

    override fun setOnTextChangedListener(listener: ((String) -> Unit)?) {
        this.textChangedListener = listener
    }

    override fun onUnbind() {
        this.textChangedListener = null
    }

}
