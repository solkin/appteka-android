package com.tomclaw.appsend.screen.unlink

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface UnlinkPresenter {

    fun attachView(view: UnlinkView)

    fun detachView()

    fun attachRouter(router: UnlinkRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface UnlinkRouter {

        fun leaveScreen()

    }

}

class UnlinkPresenterImpl(
    private val appId: String,
    private val interactor: UnlinkInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UnlinkPresenter {

    private var view: UnlinkView? = null
    private var router: UnlinkPresenter.UnlinkRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: UnlinkView) {
        this.view = view
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: UnlinkPresenter.UnlinkRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
    }

    override fun onBackPressed() {
    }

}
