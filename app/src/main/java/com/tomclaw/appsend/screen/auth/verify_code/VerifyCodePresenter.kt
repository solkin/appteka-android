package com.tomclaw.appsend.screen.auth.verify_code

import android.os.Bundle
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import retrofit2.HttpException

interface VerifyCodePresenter {

    fun attachView(view: VerifyCodeView)

    fun detachView()

    fun attachRouter(router: VerifyCodeRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface VerifyCodeRouter {

        fun leaveScreen(success: Boolean)

    }

}

class VerifyCodePresenterImpl(
    private val email: String,
    private val requestId: String,
    private val registered: Boolean,
    private val codeRegex: Regex,
    private val nameRegex: Regex,
    private val resourceProvider: VerifyCodeResourceProvider,
    private val interactor: VerifyCodeInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : VerifyCodePresenter {

    private var view: VerifyCodeView? = null
    private var router: VerifyCodePresenter.VerifyCodeRouter? = null

    private var code: String = state?.getString(KEY_CODE).orEmpty()
    private var name: String? = state?.getString(KEY_NAME)

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: VerifyCodeView) {
        this.view = view

        if (registered) {
            view.hideNameInput()
            view.setSubmitButtonText(resourceProvider.getLoginButtonText())
        } else {
            view.showNameInput()
            view.setSubmitButtonText(resourceProvider.getRegisterButtonText())
        }
        view.setCode(code)
        view.setName(name.orEmpty())
        bindButtonState()
        view.setCodeSentDescription(resourceProvider.formatCodeSentDescription(email))
        subscriptions += view.codeChanged().subscribe {
            code = it
            bindButtonState()
        }
        subscriptions += view.nameChanged().subscribe {
            name = it
            bindButtonState()
        }

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.submitClicks().subscribe { onSubmitClicked() }
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: VerifyCodePresenter.VerifyCodeRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putString(KEY_CODE, code)
        putString(KEY_NAME, name)
    }

    private fun bindButtonState() {
        if (isCodeOk() && isNameOk()) {
            view?.enableSubmitButton()
        } else {
            view?.disableSubmitButton()
        }
    }

    private fun isCodeOk(): Boolean {
        return code.isNotBlank() && code.matches(codeRegex)
    }

    private fun isNameOk(): Boolean {
        return registered || (!name.isNullOrBlank() && name?.matches(nameRegex).orNot())
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun onSubmitClicked() {
        view?.showProgress()
        subscriptions += interactor.verifyCode(requestId, code, name)
            .observeOn(schedulers.mainThread())
            .subscribe({
                view?.showContent()
                router?.leaveScreen(true)
            }, {
                view?.showContent()
                when (it) {
                    is HttpException -> {
                        if (it.code() == 429) {
                            view?.showError(resourceProvider.getRateLimitError())
                        } else {
                            view?.showError(resourceProvider.getServiceError())
                        }
                    }

                    else -> view?.showError(resourceProvider.getNetworkError())
                }
            })
    }

}

private fun Boolean?.orNot(): Boolean {
    return this ?: false
}

private const val KEY_CODE = "code"
private const val KEY_NAME = "name"
