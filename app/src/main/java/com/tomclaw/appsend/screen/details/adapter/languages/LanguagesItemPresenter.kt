package com.tomclaw.appsend.screen.details.adapter.languages

import com.tomclaw.appsend.util.adapter.ItemPresenter

class LanguagesItemPresenter : ItemPresenter<LanguagesItemView, LanguagesItem> {

    override fun bindView(view: LanguagesItemView, item: LanguagesItem, position: Int) {
        view.showLanguages(item.languages.map { it.name })
    }

}
