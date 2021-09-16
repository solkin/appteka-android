package com.tomclaw.appsend.screen.moderation

import android.os.Bundle
import android.util.LongSparseArray
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.vika.screen.home.adapter.ItemClickListener
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface ModerationPresenter : ItemClickListener {

    fun attachView(view: ModerationView)

    fun detachView()

    fun attachRouter(router: ModerationRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    interface ModerationRouter {

        fun openModerationScreen(appId: Int)

        fun leaveScreen()

    }

}

class ModerationPresenterImpl(
    private val interactor: ModerationInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ModerationPresenter {

    private var view: ModerationView? = null
    private var router: ModerationPresenter.ModerationRouter? = null

    private val subscriptions = CompositeDisposable()

    private val chatIds = LongSparseArray<AppEntity>()

    override fun attachView(view: ModerationView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }

        loadChats()
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ModerationPresenter.ModerationRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
    }

    private fun loadChats() {
        subscriptions += interactor.listApps()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { view?.showContent() }
            .subscribe(
                { onLoaded(it) },
                { onError(it) }
            )
    }

    private fun onLoaded(chats: List<AppEntity>) {
        val items = chats.asSequence()
//            .sortedBy { it.lastMsg?.time ?: 0 }
            .map {
                val item = appConverter.convert(it)
                chatIds.put(item.id, it)
                item
            }
            .toList()
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
        view?.contentUpdated()
    }

    private fun onError(it: Throwable) {}

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun onItemClick(item: Item) {
//        val chat = chatIds[item.id] ?: return
//        router?.openModerationScreen(chat.appId)
    }

}
