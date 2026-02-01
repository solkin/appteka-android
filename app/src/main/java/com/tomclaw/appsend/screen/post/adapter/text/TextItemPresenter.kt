package com.tomclaw.appsend.screen.post.adapter.text

import com.tomclaw.appsend.util.adapter.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener

class TextItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<TextItemView, TextItem> {

    override fun bindView(view: TextItemView, item: TextItem, position: Int) {
        with(view) {
            setMaxLength(item.maxLength)
            setText(item.text)
            if (item.errorRequiredField) {
                showRequiredFieldError()
            } else {
                hideRequiredFieldError()
            }
            setOnTextChangedListener { listener.onTextChanged(it) }
        }
    }

}
