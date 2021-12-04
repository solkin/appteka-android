package com.tomclaw.appsend.screen.chat

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.moderation.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy

interface ChatPresenter : ItemListener {

    fun attachView(view: ChatView)

    fun detachView()

    fun attachRouter(router: ChatRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface ChatRouter {

        fun leaveScreen()

    }

}

class ChatPresenterImpl(
    private val interactor: ChatInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ChatPresenter {

    override fun attachView(view: ChatView) {
    }

    override fun detachView() {
    }

    override fun attachRouter(router: ChatPresenter.ChatRouter) {
    }

    override fun detachRouter() {
    }

    override fun saveState(): Bundle {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
    }

    override fun onItemClick(item: Item) {
    }

    override fun onRetryClick(item: Item) {
    }

    override fun onLoadMore(item: Item) {
    }
}