package com.tomclaw.appsend.screen.users.adapter.subscriber

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.screen.users.adapter.ItemListener
import com.tomclaw.appsend.screen.users.adapter.UsersResourceProvider
import java.util.Locale

class SubscriberItemPresenter(
    private val locale: Locale,
    private val resourceProvider: UsersResourceProvider,
    private val listener: ItemListener,
) : ItemPresenter<SubscriberItemView, SubscriberItem> {

    override fun bindView(view: SubscriberItemView, item: SubscriberItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                listener.onLoadMore(this)
            }
        }

        val name = item.user.name.takeIf { !it.isNullOrBlank() }
            ?: item.user.userIcon.label[locale.language]
            ?: item.user.userIcon.label[DEFAULT_LOCALE].orEmpty()
        view.setUserName(name)
        view.setUserIcon(item.user.userIcon)
        view.setSubscribedDate(resourceProvider.formatSubscribedDate(item.time))
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        view.setOnClickListener { listener.onItemClick(item) }
    }

}
