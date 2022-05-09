package com.tomclaw.appsend.screen.details

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface DetailsPresenter {

    fun attachView(view: DetailsView)

    fun detachView()

    fun attachRouter(router: DetailsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface DetailsRouter {

        fun leaveScreen()

    }

}

class DetailsPresenterImpl(
    private val interactor: DetailsInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DetailsPresenter {

    private var view: DetailsView? = null
    private var router: DetailsPresenter.DetailsRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: DetailsView) {
        this.view = view
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: DetailsPresenter.DetailsRouter) {
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
