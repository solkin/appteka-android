package com.tomclaw.appsend.screen.moderation

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.moderation.adapter.app.AppItem
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.vika.screen.home.adapter.ItemClickListener
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.ArrayList

interface ModerationPresenter : ItemClickListener {

    fun attachView(view: ModerationView)

    fun detachView()

    fun attachRouter(router: ModerationRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    interface ModerationRouter {

        fun openAppModerationScreen(appId: Int)

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

    private var items: List<AppItem>? = state?.getParcelableArrayList(KEY_APPS)

    override fun attachView(view: ModerationView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }

        items?.let { onReady() } ?: loadApps()
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
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
    }

    private fun loadApps() {
        subscriptions += interactor.listApps()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onError(it) }
            )
    }

    private fun onLoaded(entities: List<AppEntity>) {
        items = entities.asSequence()
            .map { appConverter.convert(it) }
            .toList()
    }

    private fun onReady() {
        val items = this.items
        if (items.isNullOrEmpty()) {
            view?.showPlaceholder()
        } else {
            val dataSource = ListDataSource(items)
            adapterPresenter.get().onDataSourceChanged(dataSource)
            view?.contentUpdated()
            view?.showContent()
        }
    }

    private fun onError(it: Throwable) {
        view?.showError()
    }

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

private const val KEY_APPS = "apps"
