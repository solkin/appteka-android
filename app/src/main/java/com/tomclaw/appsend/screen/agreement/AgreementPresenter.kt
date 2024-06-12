package com.tomclaw.appsend.screen.agreement

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import retrofit2.HttpException

interface AgreementPresenter {

    fun attachView(view: AgreementView)

    fun detachView()

    fun attachRouter(router: AgreementRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface AgreementRouter {

        fun leaveScreen(success: Boolean)

    }

}

class AgreementPresenterImpl(
    state: Bundle?
) : AgreementPresenter {

    private var view: AgreementView? = null
    private var router: AgreementPresenter.AgreementRouter? = null

    private var agreed: Boolean = state?.getBoolean(KEY_AGREED) ?: false

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: AgreementView) {
        this.view = view

        view.setAgreed(agreed)
        bindButtonState()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.agreementClicks().subscribe {
            agreed = it
            bindButtonState()
        }
        subscriptions += view.submitClicks().subscribe { onSubmitClicked() }
    }

    private fun bindButtonState() {
        if (agreed) {
            view?.enableSubmitButton()
        } else {
            view?.disableSubmitButton()
        }
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: AgreementPresenter.AgreementRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putBoolean(KEY_AGREED, agreed)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun onSubmitClicked() {
        router?.leaveScreen(success = true)
    }

}

private const val KEY_AGREED = "agreed"
