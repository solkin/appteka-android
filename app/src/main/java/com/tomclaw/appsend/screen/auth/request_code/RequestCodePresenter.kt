package com.tomclaw.appsend.screen.auth.request_code

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface RequestCodePresenter {

    fun attachView(view: RequestCodeView)

    fun detachView()

    fun attachRouter(router: RequestCodeRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface RequestCodeRouter {

        fun leaveScreen(success: Boolean)

    }

}

class RequestCodePresenterImpl(
    private val interactor: RequestCodeInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : RequestCodePresenter {

    private var view: RequestCodeView? = null
    private var router: RequestCodePresenter.RequestCodeRouter? = null

    private var email: String = state?.getString(KEY_EMAIL).orEmpty()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: RequestCodeView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe { invalidate() }
    }

    private fun invalidate() {

    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: RequestCodePresenter.RequestCodeRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

}

private const val KEY_EMAIL = "email"
