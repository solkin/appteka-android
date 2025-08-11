package com.tomclaw.appsend.screen.unlink

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface UnlinkPresenter {

    fun attachView(view: UnlinkView)

    fun detachView()

    fun attachRouter(router: UnlinkRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface UnlinkRouter {

        fun leaveScreen(success: Boolean)

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

    private var reason: String = state?.getString(KEY_REASON).orEmpty()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: UnlinkView) {
        this.view = view
        view.setReason(reason)
        subscriptions += view.reasonChanged().subscribe {
            reason = it
        }
        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.submitClicks().subscribe { onSubmitClicked() }
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
        putString(KEY_REASON, reason)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun onSubmitClicked() {
        view?.showProgress()
        subscriptions += interactor.unlink(appId, reason)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { view?.showContent() }
            .subscribe(
                { router?.leaveScreen(success = true) },
                { view?.showUnlinkFailed() }
            )
    }

}

private const val KEY_REASON = "reason"
