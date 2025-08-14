package com.tomclaw.appsend.screen.unpublish

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface UnpublishPresenter {

    fun attachView(view: UnpublishView)

    fun detachView()

    fun attachRouter(router: UnpublishRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface UnpublishRouter {

        fun leaveScreen(success: Boolean)

    }

}

class UnpublishPresenterImpl(
    private val appId: String,
    private val interactor: UnpublishInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UnpublishPresenter {

    private var view: UnpublishView? = null
    private var router: UnpublishPresenter.UnpublishRouter? = null

    private var reason: String = state?.getString(KEY_REASON).orEmpty()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: UnpublishView) {
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

    override fun attachRouter(router: UnpublishPresenter.UnpublishRouter) {
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
        subscriptions += interactor.unpublish(appId, reason)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { view?.showContent() }
            .subscribe(
                { router?.leaveScreen(success = true) },
                { view?.showUnpublishFailed() }
            )
    }

}

private const val KEY_REASON = "reason"
