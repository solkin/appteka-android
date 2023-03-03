package com.tomclaw.appsend.screen.upload.adapter.whats_new

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface WhatsNewItemView : ItemView {

    fun setText(text: String)

    fun setOnTextChangedListener(listener: ((String) -> Unit)?)

}

@Suppress("DEPRECATION")
class WhatsNewItemViewHolder(view: View) : BaseViewHolder(view), WhatsNewItemView {

    private val context = view.context
    private val whatsNewEdit: EditText = view.findViewById(R.id.whats_new)

    private var textChangedListener: ((String) -> Unit)? = null

    init {
        whatsNewEdit.addTextChangedListener(object : TextWatcher {
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
        whatsNewEdit.setText(text)
    }

    override fun setOnTextChangedListener(listener: ((String) -> Unit)?) {
        this.textChangedListener = listener
    }

    override fun onUnbind() {
        this.textChangedListener = null
    }

}
