package com.tomclaw.appsend.screen.profile.adapter.header

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import java.util.Locale

class HeaderItemPresenter(
    private val listener: ItemListener,
    private val resourceProvider: HeaderResourceProvider,
    private val locale: Locale,
) : ItemPresenter<HeaderItemView, HeaderItem> {

    override fun bindView(view: HeaderItemView, item: HeaderItem, position: Int) {
        view.setUserRole(resourceProvider.getRoleName(item.role))

        view.setUserIcon(item.userIcon)

        val name = item.userName.takeIf { !it.isNullOrBlank() }
            ?: item.userIcon.label[locale.language]
            ?: item.userIcon.label[DEFAULT_LOCALE].orEmpty()
        view.setUserName(name)
    }

}
