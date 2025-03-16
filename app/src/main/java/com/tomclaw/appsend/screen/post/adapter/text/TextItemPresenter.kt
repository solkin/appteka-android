package com.tomclaw.appsend.screen.post.adapter.text

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.post.adapter.ItemListener

class TextItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<TextItemView, TextItem> {

    override fun bindView(view: TextItemView, item: TextItem, position: Int) {
        with(view) {
            setText(item.text)
            setOnTextChangedListener { listener.onTextChanged(it) }
        }
    }

}
