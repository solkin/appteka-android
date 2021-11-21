package com.tomclaw.appsend.screen.discuss

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.moderation.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.ArrayList

interface DiscussPresenter : ItemListener {

    fun attachView(view: DiscussView)

    fun detachView()

    fun attachRouter(router: DiscussPresenter.DiscussRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface DiscussRouter {
    }

}

class DiscussPresenterImpl(
    interactor: DiscussInteractor,
    adapterPresenter: Lazy<AdapterPresenter>,
    schedulers: SchedulersFactory,
    state: Bundle?
) : DiscussPresenter {

    private var view: DiscussView? = null
    private var router: DiscussPresenter.DiscussRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: DiscussView) {
        this.view = view
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: DiscussPresenter.DiscussRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
    }

    override fun onItemClick(item: Item) {
        TODO("Not yet implemented")
    }

    override fun onRetryClick(item: Item) {
        TODO("Not yet implemented")
    }

    override fun onLoadMore(item: Item) {
        TODO("Not yet implemented")
    }

}