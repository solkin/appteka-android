package com.tomclaw.appsend.screen.details

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItem
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItem
import com.tomclaw.appsend.screen.details.adapter.play.PlayItem
import com.tomclaw.appsend.screen.details.adapter.rating.RatingItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.NOT_INSTALLED
import com.tomclaw.appsend.util.PackageObserver
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

        fun openProfile(userId: Int)

    }

}

class DetailsPresenterImpl(
    private val appId: String?,
    private val packageName: String?,
    private val interactor: DetailsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val packageObserver: PackageObserver,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DetailsPresenter {

    private var view: DetailsView? = null
    private var router: DetailsPresenter.DetailsRouter? = null

    private var details: Details? = state?.getParcelable(KEY_DETAILS)
    private var installedVersionCode: Int = state?.getInt(KEY_INSTALLED_VERSION) ?: NOT_INSTALLED

    private val subscriptions = CompositeDisposable()
    private val observerSubscription = CompositeDisposable()

    override fun attachView(view: DetailsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }

        if (details != null) {
            triggerDetailsChanged()
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
        triggerDetailsChanged()
    }

    private fun triggerDetailsChanged() {
        val packageName = details?.info?.packageName ?: return
        observerSubscription.clear()
        observerSubscription += packageObserver.observe(packageName)
            .observeOn(schedulers.mainThread())
            .subscribeOn(schedulers.io())
            .subscribe(
                { installedVersionCode ->
                    this.installedVersionCode = installedVersionCode
                    bindDetails()
                    view?.showContent()
                }, {}
            )
    }

    private fun bindDetails() {
        val details = this.details ?: return

        var id: Long = 1

        val items = ArrayList<Item>()
        items += HeaderItem(
            id = id++,
            icon = details.info.icon,
            packageName = details.info.packageName,
            label = details.info.label.orEmpty(),
            userId = details.info.userId,
            userIcon = details.info.userIcon,
            userName = details.info.userName
        )
        items += PlayItem(
            id = id++,
            rating = details.meta?.rating,
            downloads = details.info.downloads ?: 0,
            size = details.info.size,
            exclusive = details.meta?.exclusive ?: false,
            category = details.meta?.category,
            osVersion = details.info.androidVersion,
            minSdk = details.info.sdkVersion,
        )
        items += ControlsItem(
            id = id++,
            appId = details.info.appId,
            packageName = details.info.packageName,
            versionCode = details.info.versionCode,
            sdkVersion = details.info.sdkVersion,
            androidVersion = details.info.androidVersion,
            size = details.info.size,
            link = details.link,
            expiresIn = details.expiresIn,
            installedVersionCode = installedVersionCode,
        )
        if (!details.meta?.description.isNullOrBlank()) {
            items += DescriptionItem(
                id = id++,
                text = details.meta?.description.orEmpty().trim(),
                versionName = details.info.version,
                versionCode = details.info.versionCode,
                uploadDate = details.info.time,
                checksum = details.info.sha1,
            )
        }
        if (!details.info.permissions.isNullOrEmpty()) {
            items += PermissionsItem(
                id = id++,
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
                id = id++,
                rateCount = details.meta.rateCount,
                rating = details.meta.rating,
                scores = details.meta.scores
            )
        }

        if (!details.ratingsList.isNullOrEmpty()) {
            items += details.ratingsList.map { rating ->
                RatingItem(
                    id++,
                    rating.score,
                    rating.text,
                    rating.time,
                    rating.userId,
                    rating.userIcon
                )
            }
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

    override fun onProfileClick(userId: Int) {
        router?.openProfile(userId)
    }

    override fun onPermissionsClick(permissions: List<String>) {
        router?.openPermissionsScreen(permissions)
    }

    override fun onScoresClick() {
        details?.info?.appId?.let { appId ->
            router?.openRatingsScreen(appId)
        }
    }

    override fun onInstallClick(appId: String) {
    }

}

private const val KEY_DETAILS = "details"
private const val KEY_INSTALLED_VERSION = "versionCode"
