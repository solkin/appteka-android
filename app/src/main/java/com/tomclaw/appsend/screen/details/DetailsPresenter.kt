package com.tomclaw.appsend.screen.details

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItem
import com.tomclaw.appsend.screen.details.adapter.play.PlayItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface DetailsPresenter : ItemListener {

    fun attachView(view: DetailsView)

    fun detachView()

    fun attachRouter(router: DetailsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface DetailsRouter {

        fun leaveScreen()

        fun openPermissionsScreen(permissions: List<String>)

        fun openRatingsScreen(appId: String)

    }

}

class DetailsPresenterImpl(
    private val appId: String?,
    private val packageName: String?,
    private val interactor: DetailsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
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

        val items = ArrayList<Item>()
        items += HeaderItem(
            id = 1,
            icon = details.info.icon,
            packageName = details.info.packageName,
            label = details.info.label.orEmpty(),
            userId = details.info.userId,
            userIcon = details.info.userIcon,
            userName = "name"
        )
        items += PlayItem(
            id = 2,
            rating = details.meta?.rating,
            downloads = details.info.downloads ?: 0,
            size = details.info.size,
            exclusive = details.meta?.exclusive ?: false,
            category = details.meta?.category,
            osVersion = details.info.androidVersion,
            minSdk = details.info.sdkVersion,
        )
        if (!details.meta?.description.isNullOrBlank()) {
            items += DescriptionItem(
                id = 3,
                text = details.meta?.description.orEmpty(),
                versionName = details.info.version,
                versionCode = details.info.versionCode,
                uploadDate = details.info.time,
                checksum = details.info.sha1,
            )
        }
        if (!details.info.permissions.isNullOrEmpty()) {
            items += PermissionsItem(
                id = 4,
                permissions = details.info.permissions,
            )
        }
        if (
            details.meta?.scores != null &&
            details.meta.rating != null &&
            details.meta.rateCount != null &&
            details.meta.rateCount > 0
        ) {
            items += ScoresItem(
                id = 5,
                rateCount = details.meta.rateCount,
                rating = details.meta.rating,
                scores = details.meta.scores
            )
        }

        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)

        view?.contentUpdated()
    }

    private fun onLoadingError() {
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onPermissionsClick(permissions: List<String>) {
        router?.openPermissionsScreen(permissions)
    }

    override fun onScoresClick() {
        details?.info?.appId?.let { appId ->
            router?.openRatingsScreen(appId)
        }
    }

}

private const val KEY_DETAILS = "details"
