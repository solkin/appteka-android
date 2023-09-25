package com.tomclaw.appsend.screen.auth.verify_code

import android.os.Bundle
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
    private val codeRegex: String,
    private val nameRegex: String,
    private val resourceProvider: VerifyCodeResourceProvider,
    private val interactor: VerifyCodeInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : VerifyCodePresenter {

    private var view: VerifyCodeView? = null
    private var router: VerifyCodePresenter.VerifyCodeRouter? = null

    private var code: String = state?.getString(KEY_CODE).orEmpty()
    private var name: String? = state?.getString(KEY_NAME)
    private var actCodeRegex: String = state?.getString(KEY_CODE_REGEX) ?: NON_EMPTY_REGEX
    private var actNameRegex: String = state?.getString(KEY_NAME_REGEX) ?: NON_EMPTY_REGEX

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: VerifyCodeView) {
        this.view = view

        if (registered) {
            view.hideNameInput()
            view.setSubmitButtonText(resourceProvider.loginButtonText())
        } else {
            view.showNameInput()
            view.setSubmitButtonText(resourceProvider.registerButtonText())
        }
        view.setCode(code)
        view.setName(name.orEmpty())
        highlightErrors()
        view.setCodeSentDescription(resourceProvider.formatCodeSentDescription(email))
        subscriptions += view.codeChanged().subscribe {
            code = it
            highlightCodeError()
        }
        subscriptions += view.nameChanged().subscribe {
            name = it
            highlightNameError()
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
        putString(KEY_CODE_REGEX, actCodeRegex)
        putString(KEY_NAME_REGEX, actNameRegex)
    }

    private fun setStrictRegex() {
        actCodeRegex = codeRegex
        actNameRegex = nameRegex
    }

    private fun highlightErrors() {
        highlightCodeError()
        highlightNameError()
    }

    private fun highlightCodeError() {
        view?.setCodeError(if (isCodeOk()) "" else resourceProvider.codeFormatInvalid())
    }

    private fun highlightNameError() {
        view?.setNameError(if (isNameOk()) "" else resourceProvider.nameFormatInvalid())
    }

    private fun isCodeOk(): Boolean {
        return code.matches(actCodeRegex.toRegex())
    }

    private fun isNameOk(): Boolean {
        return registered || name.orEmpty().matches(actNameRegex.toRegex())
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun onSubmitClicked() {
        setStrictRegex()
        highlightErrors()
        if (!isCodeOk() || !isNameOk()) {
            return
        }

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
                            view?.showError(resourceProvider.rateLimitError())
                        } else {
                            view?.showError(resourceProvider.serviceError())
                        }
                    }

                    else -> view?.showError(resourceProvider.networkError())
                }
            })
    }

}

private const val KEY_CODE = "code"
private const val KEY_NAME = "name"
private const val KEY_CODE_REGEX = "code_regex"
private const val KEY_NAME_REGEX = "name_regex"

private const val NON_EMPTY_REGEX = "(.*?)|^$"
