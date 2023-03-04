package com.tomclaw.appsend.screen.upload.adapter.description

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R

interface DescriptionItemView : ItemView {

    fun setText(text: String)

    fun setOnTextChangedListener(listener: ((String) -> Unit)?)

}

@Suppress("DEPRECATION")
class DescriptionItemViewHolder(view: View) : BaseViewHolder(view), DescriptionItemView {

    private val context = view.context
    private val descriptionEdit: EditText = view.findViewById(R.id.description)

    private var textChangedListener: ((String) -> Unit)? = null

    init {
        descriptionEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                textChangedListener?.invoke(s.toString())
            }
        })
    }

    override fun setText(text: String) {
        descriptionEdit.setText(text)
    }

    override fun setOnTextChangedListener(listener: ((String) -> Unit)?) {
        this.textChangedListener = listener
    }

    override fun onUnbind() {
        this.textChangedListener = null
    }

}
