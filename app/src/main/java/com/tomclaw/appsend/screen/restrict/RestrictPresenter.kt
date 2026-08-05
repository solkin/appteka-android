package com.tomclaw.appsend.screen.restrict

import android.os.Bundle
import com.tomclaw.appsend.dto.RejectionReason
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterCapabilityErrors
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface RestrictPresenter {

    fun attachView(view: RestrictView)

    fun detachView()

    fun attachRouter(router: RestrictRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface RestrictRouter {

        fun leaveScreen(success: Boolean)

    }

}

typealias RestrictRouter = RestrictPresenter.RestrictRouter

class RestrictPresenterImpl(
    private val appId: String,
    private val interactor: RestrictInteractor,
    private val resourceProvider: RestrictResourceProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : RestrictPresenter {

    private var view: RestrictView? = null
    private var router: RestrictRouter? = null

    private var reasons: List<RejectionReason> = emptyList()
    private var reason: RejectionReason? = state?.getParcelable(KEY_REASON)
    private var comment: String = state?.getString(KEY_COMMENT).orEmpty()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: RestrictView) {
        this.view = view
        view.setDescription(resourceProvider.description())
        view.setSubmitLabel(resourceProvider.submitLabel())
        view.setWarning(resourceProvider.warning())
        view.setComment(comment)
        bindReason()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.reasonClicks().subscribe { onReasonClicked() }
        subscriptions += view.reasonPicked().subscribe { picked ->
            reason = picked
            bindReason()
        }
        subscriptions += view.commentChanged().subscribe {
            comment = it
            bindSubmitState()
        }
        subscriptions += view.submitClicks().subscribe { onSubmitClicked() }

        if (reasons.isEmpty()) {
            loadReasons()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: RestrictRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelable(KEY_REASON, reason)
        putString(KEY_COMMENT, comment)
    }

    override fun onBackPressed() {
        router?.leaveScreen(success = false)
    }

    private fun bindReason() {
        view?.setReason(reason?.text)
        bindSubmitState()
    }

    // Submitting needs a reason, and a comment on top of it whenever the
    // catalog says that reason is meaningless without one.
    private fun bindSubmitState() {
        val picked = reason
        val ready = picked != null && (!picked.requiresComment || comment.isNotBlank())
        view?.setSubmitEnabled(ready)
    }

    private fun loadReasons() {
        subscriptions += interactor.reasons()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { reasons = it },
                { ex ->
                    ex.filterCapabilityErrors(
                        authError = { view?.showUnauthorizedError() },
                        capabilityDenied = { cap -> view?.showCapabilityDenied(cap) },
                        other = { view?.showFailed() },
                    )
                }
            )
    }

    private fun onReasonClicked() {
        if (reasons.isEmpty()) {
            loadReasons()
            return
        }
        view?.showReasonPicker(reasons)
    }

    private fun onSubmitClicked() {
        val picked = reason ?: return
        view?.showProgress()
        subscriptions += interactor.submit(appId, picked.code, comment.trim())
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doAfterTerminate { view?.showContent() }
            .subscribe(
                { router?.leaveScreen(success = true) },
                { ex ->
                    ex.filterCapabilityErrors(
                        authError = { view?.showUnauthorizedError() },
                        capabilityDenied = { cap -> view?.showCapabilityDenied(cap) },
                        other = { view?.showFailed() },
                    )
                }
            )
    }

}

private const val KEY_REASON = "reason"
private const val KEY_COMMENT = "comment"
