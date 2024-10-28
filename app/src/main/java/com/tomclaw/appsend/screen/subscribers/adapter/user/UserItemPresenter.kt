package com.tomclaw.appsend.screen.subscribers.adapter.user

import com.avito.konveyor.blueprint.ItemPresenter
import com.tomclaw.appsend.screen.subscribers.adapter.ItemListener

class UserItemPresenter(
    private val listener: ItemListener,
) : ItemPresenter<UserItemView, UserItem> {

    override fun bindView(view: UserItemView, item: UserItem, position: Int) {
        with(item) {
            if (hasMore) {
                hasMore = false
                hasProgress = true
                hasError = false
                listener.onLoadMore(this)
            }
        }

        var statusText = ""
        var isPublished = false
        view.setStatus(statusText, isPublished)
        if (item.hasProgress) view.showProgress() else view.hideProgress()
        if (item.hasError) view.showError() else view.hideError()
        view.setOnRetryListener { listener.onRetryClick(item) }
    }

}
