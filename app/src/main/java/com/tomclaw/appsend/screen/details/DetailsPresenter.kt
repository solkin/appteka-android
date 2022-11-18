package com.tomclaw.appsend.screen.details

import android.os.Build
import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.download.IDLE
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.controls.ControlsItem
import com.tomclaw.appsend.screen.details.adapter.description.DescriptionItem
import com.tomclaw.appsend.screen.details.adapter.header.HeaderItem
import com.tomclaw.appsend.screen.details.adapter.permissions.PermissionsItem
import com.tomclaw.appsend.screen.details.adapter.play.PlayItem
import com.tomclaw.appsend.screen.details.adapter.rating.RatingItem
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.adapter.user_rate.UserRateItem
import com.tomclaw.appsend.screen.details.adapter.user_review.UserReviewItem
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.NOT_INSTALLED
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import retrofit2.HttpException
import java.io.File
import java.util.concurrent.TimeUnit

interface DetailsPresenter : ItemListener {

    fun attachView(view: DetailsView)

    fun detachView()

    fun attachRouter(router: DetailsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun showSnackbar(text: String)

    fun invalidateDetails()

    interface DetailsRouter {

        fun leaveScreen()

        fun leaveModeration()

        fun requestStoragePermissions(callback: () -> Unit)

        fun openPermissionsScreen(permissions: List<String>)

        fun openRatingsScreen(appId: String)

        fun openProfile(userId: Int)

        fun launchApp(packageName: String)

        fun installApp(file: File)

        fun removeApp(packageName: String)

        fun openRateScreen(
            appId: String,
            rating: Float,
            review: String?,
            label: String?,
            icon: String?
        )

        fun openEditMetaScreen(appId: String, label: String?, icon: String?, packageName: String)

        fun openUnpublishScreen(appId: String, label: String?)

        fun openUnlinkScreen(appId: String, label: String?)

        fun openAbuseScreen(appId: String, label: String?)

        fun openDetailsScreen(appId: String, label: String?)

        fun openStoreScreen()

        fun startDownload(label: String, version: String, icon: String?, appId: String, url: String)

    }

}

class DetailsPresenterImpl(
    private val appId: String?,
    private val packageName: String?,
    private val moderation: Boolean,
    private val finishOnly: Boolean,
    private val interactor: DetailsInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val packageObserver: PackageObserver,
    private val downloadManager: DownloadManager,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DetailsPresenter {

    private var view: DetailsView? = null
    private var router: DetailsPresenter.DetailsRouter? = null

    private var details: Details? = state?.getParcelable(KEY_DETAILS)
    private var installedVersionCode: Int = state?.getInt(KEY_INSTALLED_VERSION) ?: NOT_INSTALLED
    private var downloadState: Int = state?.getInt(KEY_DOWNLOAD_STATE) ?: IDLE
    private var targetFile: File? = state?.getString(KEY_TARGET_FILE)?.let { File(it) }
    private var needInstall: Boolean = state?.getBoolean(KEY_NEED_INSTALL) ?: false

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val observerSubscription = CompositeDisposable()

    override fun attachView(view: DetailsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.editClicks().subscribe {
            appId?.let { appId ->
                val info = details?.info ?: return@subscribe
                router?.openEditMetaScreen(appId, info.label, info.icon, info.packageName)
            }
        }
        subscriptions += view.unpublishClicks().subscribe {
            appId?.let { appId -> router?.openUnpublishScreen(appId, details?.info?.label) }
        }
        subscriptions += view.unlinkClicks().subscribe {
            appId?.let { appId -> router?.openUnlinkScreen(appId, details?.info?.label) }
        }
        subscriptions += view.deleteClicks().subscribe {
            // TODO: add delete app impl
        }
        subscriptions += view.abuseClicks().subscribe {
            appId?.let { appId -> router?.openAbuseScreen(appId, details?.info?.label) }
        }
        subscriptions += view.versionClicks().subscribe { version ->
            details?.let { details ->
                router?.leaveScreen()
                router?.openDetailsScreen(version.appId, details.info.label)
            }
        }
        subscriptions += view.moderationClicks().subscribe { isApprove ->
            sendModerationDecision(isApprove)
        }
        subscriptions += view.retryClicks().subscribe {
            loadDetails()
        }

        if (moderation) {
            view.showModeration()
        }

        if (details != null) {
            triggerDetailsChanged()
        } else {
            loadDetails()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        observerSubscription.clear()
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
        putInt(KEY_INSTALLED_VERSION, installedVersionCode)
        putInt(KEY_DOWNLOAD_STATE, downloadState)
        putString(KEY_TARGET_FILE, targetFile?.absolutePath)
        putBoolean(KEY_NEED_INSTALL, needInstall)
    }

    private fun loadDetails() {
        subscriptions += interactor.loadDetails(appId, packageName)
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    if (it is HttpException) {
                        throw it
                    }
                    println("[details] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
            .doOnSubscribe {
                view?.hideMenu()
                view?.hideError()
                view?.showProgress()
            }
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
        val appId = details?.info?.appId ?: return
        observerSubscription.clear()
        observerSubscription += packageObserver.observe(packageName)
            .map { installedVersionCode ->
                this.installedVersionCode = installedVersionCode
            }
            .flatMap { downloadManager.status(appId) }
            .map { downloadState ->
                this.downloadState = downloadState
            }
            .observeOn(schedulers.mainThread())
            .subscribeOn(schedulers.io())
            .subscribe(
                {
                    tryInstall()
                    bindDetails()
                    view?.showContent()
                }, {}
            )
    }

    private fun bindDetails() {
        val details = this.details ?: return

        var id: Long = 1

        items.clear()

        items += HeaderItem(
            id = id++,
            icon = details.info.icon,
            packageName = details.info.packageName,
            label = details.info.label.orEmpty(),
            userId = details.info.userId,
            userIcon = details.info.userIcon,
            userName = details.info.userName,
            downloadState = downloadState,
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
            downloadState = downloadState,
        )
        if (details.userRating != null) {
            items += UserReviewItem(
                id = id++,
                score = details.userRating.score,
                text = details.userRating.text,
                time = details.userRating.time * 1000,
                userId = details.userRating.userId,
                userIcon = details.userRating.userIcon,
                userName = details.userRating.userName,
            )
        } else if (installedVersionCode != NOT_INSTALLED) {
            items += UserRateItem(
                id = id++,
                appId = details.info.appId,
            )
        }
        if (!details.meta?.description.isNullOrBlank()) {
            items += DescriptionItem(
                id = id++,
                text = details.meta?.description.orEmpty().trim(),
                versionName = details.info.version,
                versionCode = details.info.versionCode,
                versionsCount = details.versions?.size ?: 0,
                uploadDate = details.info.time * 1000,
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
                    rating.time * 1000,
                    rating.userId,
                    rating.userIcon
                )
            }
        }

        bindItems()

        view?.showMenu(
            canEdit = checkAction(ACTION_EDIT_META),
            canUnlink = checkAction(ACTION_UNLINK),
            canUnpublish = checkAction(ACTION_UNPUBLISH),
            canDelete = checkAction(ACTION_DELETE)
        )

        view?.contentUpdated()
    }

    private fun bindItems() {
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
    }

    private fun tryInstall(): Boolean {
        val file = targetFile ?: return false
        if (needInstall && downloadState == COMPLETED && file.exists()) {
            router?.installApp(file)
            needInstall = false
            return true
        }
        return false
    }

    private fun checkAction(action: String, fallback: Boolean = false): Boolean {
        return details?.actions?.contains(action) ?: fallback
    }

    private fun sendModerationDecision(isApprove: Boolean) {
        val details = details ?: return
        subscriptions += interactor.sendModerationDecision(details.info.appId, isApprove)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    println("[moderation decision] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
            .doOnSubscribe {
                view?.hideMenu()
                view?.showProgress()
            }
            .subscribe(
                { onModerationDecisionSent() },
                { onLoadingError() }
            )
    }

    private fun onModerationDecisionSent() {
        router?.leaveModeration()
    }

    private fun onLoadingError() {
        view?.hideMenu()
        view?.showContent()
        view?.showError()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
        if (!finishOnly) {
            router?.openStoreScreen()
        }
    }

    override fun showSnackbar(text: String) {
        view?.showSnackbar(text)
    }

    override fun invalidateDetails() {
        loadDetails()
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

    override fun onInstallClick() {
        router?.requestStoragePermissions { onInstall() }
    }

    private fun onInstall() {
        val details = details ?: return
        needInstall = true

        router?.startDownload(
            label = details.info.label.orEmpty(),
            version = details.info.version,
            appId = details.info.appId,
            icon = details.info.icon,
            url = details.link
        )

        val file = downloadManager.targetFile(
            label = details.info.label.orEmpty(),
            version = details.info.version,
            appId = details.info.appId
        )
        this.targetFile = file
        if (tryInstall()) {
            return
        }
    }

    override fun onLaunchClick(packageName: String) {
        router?.launchApp(packageName)
    }

    override fun onRemoveClick(packageName: String) {
        router?.removeApp(packageName)
    }

    override fun onCancelClick(appId: String) {
        downloadManager.cancel(appId)
    }

    override fun onRateClick(rating: Float, review: String?) {
        val details = details ?: return
        router?.openRateScreen(
            details.info.appId,
            rating,
            review,
            details.info.label,
            details.info.icon
        )
    }

    override fun onVersionsClick() {
        val info = details?.info ?: return
        val versions = details?.versions ?: return
        val items = versions
            .sortedBy { it.verCode }
            .reversed()
            .map { version ->
                VersionItem(
                    versionId = version.appId.hashCode(),
                    appId = version.appId,
                    title = version.verName + " (" + version.verCode + ")",
                    compatible = version.sdkVersion <= Build.VERSION.SDK_INT,
                    newer = version.verCode > info.versionCode,
                )
            }
        view?.showVersionsDialog(items)
    }

}

private const val KEY_DETAILS = "details"
private const val KEY_INSTALLED_VERSION = "versionCode"
private const val KEY_DOWNLOAD_STATE = "downloadState"
private const val KEY_TARGET_FILE = "targetFile"
private const val KEY_NEED_INSTALL = "needInstall"

private const val ACTION_UNLINK = "unlink"
private const val ACTION_UNPUBLISH = "unpublish"
private const val ACTION_DELETE = "delete"
private const val ACTION_EDIT_META = "edit_meta"
