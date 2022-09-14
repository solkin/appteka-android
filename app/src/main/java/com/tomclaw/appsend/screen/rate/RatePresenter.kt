package com.tomclaw.appsend.screen.rate

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface RatePresenter {

    fun attachView(view: RateView)

    fun detachView()

    fun attachRouter(router: RateRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface RateRouter {

        fun leaveScreen()

    }

}

class RatePresenterImpl(
    private val appId: String,
    private val interactor: RateInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : RatePresenter {

    private var view: RateView? = null
    private var router: RatePresenter.RateRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: RateView) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: RatePresenter.RateRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

}
