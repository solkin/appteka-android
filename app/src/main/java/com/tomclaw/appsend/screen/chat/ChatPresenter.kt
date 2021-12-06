package com.tomclaw.appsend.screen.chat

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.chat.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable

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

    private var view: ChatView? = null
    private var router: ChatPresenter.ChatRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: ChatView) {
        this.view = view

        view.navigationClicks().subscribe {
            onBackPressed()
        }

        view.retryClicks().subscribe {
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ChatPresenter.ChatRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onItemClick(item: Item) {
    }

    override fun onLoadMore(item: Item) {
    }
}
