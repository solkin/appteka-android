package com.tomclaw.appsend.screen.change_email

import android.os.Bundle
import android.util.Patterns
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import retrofit2.HttpException

interface ChangeEmailPresenter {

    fun attachView(view: ChangeEmailView)

    fun detachView()

    fun attachRouter(router: ChangeEmailRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface ChangeEmailRouter {

        fun leaveScreen(success: Boolean)

    }

}

class ChangeEmailPresenterImpl(
    private val resourceProvider: ChangeEmailResourceProvider,
    private val interactor: ChangeEmailInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ChangeEmailPresenter {

    private var view: ChangeEmailView? = null
    private var router: ChangeEmailPresenter.ChangeEmailRouter? = null

    private var email: String = state?.getString(KEY_EMAIL).orEmpty()
    private var code: String = state?.getString(KEY_CODE).orEmpty()
    private var requestId: String? = state?.getString(KEY_REQUEST_ID)
    private var codeRegex: String = state?.getString(KEY_CODE_REGEX).orEmpty()

    private val subscriptions = CompositeDisposable()

    private val isCodeSent: Boolean get() = requestId != null

    override fun attachView(view: ChangeEmailView) {
        this.view = view

        view.setEmail(email)
        view.setCode(code)

        bindState()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.emailChanged().subscribe {
            email = it
            bindSendCodeButtonState()
        }
        subscriptions += view.codeChanged().subscribe {
            code = it
            bindConfirmButtonState()
        }
        subscriptions += view.sendCodeClicks().subscribe { onSendCodeClicked() }
        subscriptions += view.confirmClicks().subscribe { onConfirmClicked() }
    }

    private fun bindState() {
        if (isCodeSent) {
            view?.lockEmailInput()
            view?.showCodeSection(resourceProvider.getCodeSentMessage(email))
            bindConfirmButtonState()
        } else {
            view?.unlockEmailInput()
            view?.hideCodeSection()
            bindSendCodeButtonState()
        }
    }

    private fun bindSendCodeButtonState() {
        if (email.isValidEmail()) {
            view?.enableSendCodeButton()
        } else {
            view?.disableSendCodeButton()
        }
    }

    private fun bindConfirmButtonState() {
        if (code.isNotEmpty() && (codeRegex.isEmpty() || code.matches(codeRegex.toRegex()))) {
            view?.enableConfirmButton()
        } else {
            view?.disableConfirmButton()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ChangeEmailPresenter.ChangeEmailRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putString(KEY_EMAIL, email)
        putString(KEY_CODE, code)
        putString(KEY_REQUEST_ID, requestId)
        putString(KEY_CODE_REGEX, codeRegex)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun onSendCodeClicked() {
        if (!email.isValidEmail()) {
            view?.showError(resourceProvider.getInvalidEmailError())
            return
        }

        view?.showProgress()
        subscriptions += interactor.requestEmailChange(email)
            .observeOn(schedulers.mainThread())
            .subscribe({ response ->
                view?.showContent()
                requestId = response.requestId
                codeRegex = response.codeRegex
                bindState()
            }, { error ->
                view?.showContent()
                handleRequestError(error)
            })
    }

    private fun onConfirmClicked() {
        val reqId = requestId ?: return

        if (codeRegex.isNotEmpty() && !code.matches(codeRegex.toRegex())) {
            view?.showError(resourceProvider.getInvalidCodeError())
            return
        }

        view?.showProgress()
        subscriptions += interactor.verifyEmailChange(reqId, code)
            .observeOn(schedulers.mainThread())
            .subscribe({
                view?.showContent()
                view?.showSuccess(resourceProvider.getEmailChangedSuccess())
                router?.leaveScreen(success = true)
            }, { error ->
                view?.showContent()
                handleVerifyError(error)
            })
    }

    private fun handleRequestError(error: Throwable) {
        when (error) {
            is HttpException -> {
                when (error.code()) {
                    409 -> view?.showError(resourceProvider.getEmailAlreadyTakenError())
                    423 -> view?.showError(resourceProvider.getDomainBlockedError())
                    429 -> view?.showError(resourceProvider.getRateLimitError())
                    else -> view?.showError(resourceProvider.getServiceError())
                }
            }

            else -> view?.showError(resourceProvider.getNetworkError())
        }
    }

    private fun handleVerifyError(error: Throwable) {
        when (error) {
            is HttpException -> {
                when (error.code()) {
                    400 -> {
                        view?.showError(resourceProvider.getInvalidCodeError())
                    }

                    409 -> view?.showError(resourceProvider.getEmailAlreadyTakenError())
                    429 -> view?.showError(resourceProvider.getRateLimitError())
                    else -> view?.showError(resourceProvider.getServiceError())
                }
            }

            else -> view?.showError(resourceProvider.getNetworkError())
        }
    }

    private fun String?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

}

private const val KEY_EMAIL = "email"
private const val KEY_CODE = "code"
private const val KEY_REQUEST_ID = "request_id"
private const val KEY_CODE_REGEX = "code_regex"
