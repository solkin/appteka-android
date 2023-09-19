package com.tomclaw.appsend.screen.auth.request_code

import android.os.Bundle
import android.provider.SyncStateContract.Helpers
import android.util.Patterns
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

        fun showVerifyCodeScreen(email: String, registered: Boolean)

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

        view.setEmail(email)
        bindButtonState()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.emailChanged().subscribe {
            email = it
            bindButtonState()
        }
        subscriptions += view.submitClicks().subscribe { onSubmitClicked() }
    }

    private fun onSubmitClicked() {
        subscriptions += interactor.requestCode(email)
            .observeOn(schedulers.mainThread())
            .subscribe({
                router?.showVerifyCodeScreen(email, it.registered)
            }, {
                view?.showError()
            })
    }

    private fun bindButtonState() {
        if (email.isValidEmail()) {
            view?.enableSubmitButton()
        } else {
            view?.disableSubmitButton()
        }
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
        putString(KEY_EMAIL, email)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun String?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

}

private const val KEY_EMAIL = "email"
