package com.tomclaw.appsend.screen.subscriptions

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface SubscriptionsPresenter {

    fun attachView(view: SubscriptionsView)

    fun detachView()

    fun attachRouter(router: SubscriptionsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface SubscriptionsRouter {

        fun openProfileScreen(userId: Int)

        fun leaveScreen()

    }

}

class SubscriptionsPresenterImpl(
    schedulers: SchedulersFactory,
    state: Bundle?
) : SubscriptionsPresenter {

    private var view: SubscriptionsView? = null
    private var router: SubscriptionsPresenter.SubscriptionsRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: SubscriptionsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: SubscriptionsPresenter.SubscriptionsRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {}

    override fun onBackPressed() {
        router?.leaveScreen()
    }

}