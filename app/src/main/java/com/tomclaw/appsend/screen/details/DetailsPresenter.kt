package com.tomclaw.appsend.screen.details

import android.os.Bundle
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

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
    private val appId: String?,
    private val packageName: String?,
    private val interactor: DetailsInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DetailsPresenter {

    private var view: DetailsView? = null
    private var router: DetailsPresenter.DetailsRouter? = null

    private var details: Details? = state?.getParcelable(KEY_DETAILS)

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: DetailsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }

        if (details != null) {
            bindDetails()
        } else {
            loadDetails()
        }
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
        putParcelable(KEY_DETAILS, details)
    }

    private fun loadDetails() {
        subscriptions += interactor.loadDetails(appId, packageName)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { onDetailsLoaded(it) },
                { onLoadingError() }
            )
    }

    private fun onDetailsLoaded(details: Details) {
        this.details = details
        bindDetails()
        view?.showContent()
    }

    private fun bindDetails() {
        val details = this.details ?: return
        view?.let {
            with(it) {
                setAppIcon(details.info?.icon)
                setAppLabel(details.info?.label)
                setAppPackage(details.info?.packageName)
                if (details.info?.userId != null && details.info.userIcon != null) {
                    showUploader()
                    setUploaderIcon(details.info.userIcon)
                    setUploaderName(details.info.userIcon.label["en"].orEmpty()) // TODO: locale + real name
                } else {
                    hideUploader()
                }
            }
        }
    }

    private fun onLoadingError() {
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

}

private const val KEY_DETAILS = "details"
