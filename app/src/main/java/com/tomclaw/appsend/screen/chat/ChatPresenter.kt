package com.tomclaw.appsend.screen.chat

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.TopicEntry
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
    private val topicId: Int,
    private val interactor: ChatInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ChatPresenter {

    private var view: ChatView? = null
    private var router: ChatPresenter.ChatRouter? = null

    private var topic: TopicEntry? = state?.getParcelable(KEY_TOPIC)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

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
        putParcelable(KEY_TOPIC, topic)
        putBoolean(KEY_ERROR, isError)
    }

    private fun loadTopic() {
        interactor.getTopic(topicId)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { onInfoLoaded(it) },
                { onInfoError() }
            )
    }

    private fun onInfoLoaded(topic: TopicEntry) {
        view?.setTitle(topic.title)

    }

    private fun onInfoError() {
        TODO("Not yet implemented")
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onItemClick(item: Item) {
    }

    override fun onLoadMore(item: Item) {
    }
}

private const val KEY_TOPIC = "topic"
private const val KEY_ERROR = "error"
