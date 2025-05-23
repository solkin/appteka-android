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
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem
import com.tomclaw.appsend.screen.details.adapter.status.StatusAction
import com.tomclaw.appsend.screen.details.api.ACTION_DELETE
import com.tomclaw.appsend.screen.details.api.ACTION_EDIT_META
import com.tomclaw.appsend.screen.details.api.ACTION_UNLINK
import com.tomclaw.appsend.screen.details.api.ACTION_UNPUBLISH
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.TranslationResponse
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.NOT_INSTALLED
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.io.File

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
            userBrief: UserBrief,
            rating: Float,
            review: String?,
            label: String?,
            icon: String?
        )

        fun openEditMetaScreen(
            appId: String,
            label: String?,
            icon: String?,
            packageName: String,
            sha1: String
        )

        fun openUnpublishScreen(appId: String, label: String?)

        fun openUnlinkScreen(appId: String, label: String?)

        fun openAbuseScreen(appId: String, label: String?)

        fun openDetailsScreen(appId: String, label: String?)

        fun openChatScreen(topicId: Int, label: String?)

        fun openStoreScreen()

        fun openGooglePlay(packageName: String)

        fun startDownload(label: String, version: String, icon: String?, appId: String, url: String)

        fun openShare(title: String, text: String)

        fun openLoginScreen()

        fun openGallery(items: List<GalleryItem>, current: Int)

    }

}

class DetailsPresenterImpl(
    private val appId: String?,
    private val packageName: String?,
    private val moderation: Boolean,
    private val finishOnly: Boolean,
    private val interactor: DetailsInteractor,
    private val resourceProvider: DetailsResourceProvider,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val detailsConverter: DetailsConverter,
    private val packageObserver: PackageObserver,
    private val downloadManager: DownloadManager,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DetailsPresenter {

    private var view: DetailsView? = null
    private var router: DetailsPresenter.DetailsRouter? = null

    private var details: Details? = state?.getParcelableCompat(KEY_DETAILS, Details::class.java)
    private var installedVersionCode: Int = state?.getInt(KEY_INSTALLED_VERSION) ?: NOT_INSTALLED
    private var downloadState: Int = state?.getInt(KEY_DOWNLOAD_STATE) ?: IDLE
    private var targetFile: File? = state?.getString(KEY_TARGET_FILE)?.let { File(it) }
    private var needInstall: Boolean = state?.getBoolean(KEY_NEED_INSTALL) == true
    private var isFavorite: Boolean = state?.getBoolean(KEY_IS_FAVORITE) == true
    private var translationData: TranslationResponse? =
        state?.getParcelableCompat(KEY_TRANSLATION_DATA, TranslationResponse::class.java)
    private var translationState: Int = state?.getInt(KEY_TRANSLATION_STATE) ?: TRANSLATION_ORIGINAL

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val observerSubscription = CompositeDisposable()

    override fun attachView(view: DetailsView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.swipeRefresh().subscribe { invalidateDetails() }
        subscriptions += view.shareClicks().subscribe {
            val details = details ?: return@subscribe
            router?.openShare(
                title = resourceProvider.shareTitle(),
                text = resourceProvider.formatShareText(
                    details.url,
                    details.info.label,
                    details.info.labels,
                    details.info.size
                )
            )
        }
        subscriptions += view.editClicks().subscribe {
            appId?.let { appId ->
                val info = details?.info ?: return@subscribe
                router?.openEditMetaScreen(
                    appId,
                    info.label,
                    info.icon,
                    info.packageName,
                    info.sha1
                )
            }
        }
        subscriptions += view.unpublishClicks().subscribe {
            appId?.let { appId -> router?.openUnpublishScreen(appId, details?.info?.label) }
        }
        subscriptions += view.unlinkClicks().subscribe {
            appId?.let { appId -> router?.openUnlinkScreen(appId, details?.info?.label) }
        }
        subscriptions += view.deleteClicks().subscribe { isConfirmed ->
            if (!isConfirmed) {
                view.showDeletionDialog()
            } else {
                deleteFromStore()
            }
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
        subscriptions += view.retryClicks().subscribe { invalidateDetails() }
        subscriptions += view.favoriteClicks().subscribe { isFavorite ->
            markFavorite(isFavorite)
        }
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }

        if (moderation) {
            view.showModeration()
        }

        if (details != null) {
            dispatchPackageStatus()
        } else {
            loadDetails()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        observerSubscription.clear()
        view?.onDismiss()
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
        putBoolean(KEY_IS_FAVORITE, isFavorite)
        putParcelable(KEY_TRANSLATION_DATA, translationData)
        putInt(KEY_TRANSLATION_STATE, translationState)
    }

    private fun loadDetails() {
        subscriptions += interactor.loadDetails(appId, packageName)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.hideError()
                view?.showProgress()
            }
            .subscribe(
                { onDetailsLoaded(it) },
                { onLoadingError(it) }
            )
    }

    private fun onDetailsLoaded(details: Details) {
        this.details = details
        this.isFavorite = details.isFavorite == true
        dispatchPackageStatus()
    }

    private fun dispatchPackageStatus() {
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

        items.clear()
        items += detailsConverter.convert(details, downloadState, installedVersionCode, moderation, translationData, translationState)

        bindItems()

        bindMenu()

        view?.contentUpdated()
    }

    private fun bindMenu() {
        view?.showMenu(
            isFavorite = isFavorite,
            canEdit = checkAction(ACTION_EDIT_META),
            canUnlink = checkAction(ACTION_UNLINK),
            canUnpublish = checkAction(ACTION_UNPUBLISH),
            canDelete = checkAction(ACTION_DELETE)
        )
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
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.showProgress()
            }
            .subscribe(
                { onModerationDecisionSent() },
                { onLoadingError(it) }
            )
    }

    private fun onModerationDecisionSent() {
        router?.leaveModeration()
    }

    private fun markFavorite(isFavorite: Boolean) {
        val details = details ?: return
        subscriptions += interactor.markFavorite(details.info.appId, isFavorite)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onFavoriteMarked(isFavorite) },
                { onFavoriteError(it, isFavorite) }
            )
    }

    private fun onFavoriteMarked(isFavorite: Boolean) {
        this.isFavorite = isFavorite
        val text = if (isFavorite) {
            resourceProvider.markedFavorite()
        } else {
            resourceProvider.unmarkedFavorite()
        }
        bindMenu()
        view?.showSnackbar(text)
    }

    private fun onFavoriteError(ex: Throwable, isFavorite: Boolean) {
        ex.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
            val text = if (isFavorite) {
                resourceProvider.markFavoriteError()
            } else {
                resourceProvider.unmarkFavoriteError()
            }
            view?.showSnackbar(text)
        }
    }

    private fun deleteFromStore() {
        val details = details ?: return
        subscriptions += interactor.deleteApplication(details.info.appId)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.showProgress()
            }
            .subscribe(
                { onApplicationDeletedFromStore() },
                { onLoadingError(it) }
            )
    }

    private fun onApplicationDeletedFromStore() {
        router?.leaveScreen()
    }

    private fun onLoadingError(ex: Throwable) {
        ex.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
            view?.hideMenu()
            view?.showContent()
            view?.showError()
        }
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

    override fun onDiscussClick() {
        val details = details ?: return
        if (details.topicId != null) {
            router?.openChatScreen(details.topicId, details.info.label)
        } else {
            subscriptions += interactor.createTopic(details.info.packageName)
                .toObservable()
                .observeOn(schedulers.mainThread())
                .retryWhenNonAuthErrors()
                .subscribe(
                    { router?.openChatScreen(topicId = it.topic.topicId, label = it.topic.title) },
                    {
                        it.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
                            showSnackbar(resourceProvider.createTopicError())
                        }
                    }
                )
        }
    }

    override fun onTranslateClick() {
        if (translationData != null) {
            translationState = when (translationState) {
                TRANSLATION_ORIGINAL -> TRANSLATION_TRANSLATED
                TRANSLATION_TRANSLATED -> TRANSLATION_ORIGINAL
                else -> translationState
            }
            bindDetails()
            view?.showContent()
            return
        }
        val appId = appId ?: details?.info?.appId ?: return
        subscriptions += interactor.translate(appId)
            .toObservable()
            .doOnSubscribe { onTranslationStarted() }
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onTranslationLoaded(it) },
                { onTranslationError() }
            )
    }

    private fun onTranslationStarted() {
        translationState = TRANSLATION_PROGRESS
        bindDetails()
        view?.showContent()
    }

    private fun onTranslationLoaded(response: TranslationResponse) {
        translationData = response
        translationState = TRANSLATION_TRANSLATED
        bindDetails()
        view?.showContent()
    }

    private fun onTranslationError() {
        translationState = TRANSLATION_ORIGINAL
        bindDetails()
        view?.showContent()
        view?.showSnackbar(resourceProvider.translationError())
    }

    override fun onGooglePlayClick() {
        val info = details?.info ?: return
        router?.openGooglePlay(info.packageName)
    }

    override fun onRateClick(rating: Float, review: String?) {
        val details = details ?: return
        subscriptions += interactor.getUserBrief()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { userBrief ->
                    router?.openRateScreen(
                        details.info.appId,
                        userBrief,
                        rating,
                        review,
                        details.info.label,
                        details.info.icon
                    )
                },
                {
                    it.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
                        showSnackbar(resourceProvider.rateAppError())
                    }
                }
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
                    title = resourceProvider.formatVersion(version),
                    compatible = version.sdkVersion <= Build.VERSION.SDK_INT,
                    newer = version.verCode > info.versionCode,
                )
            }
        view?.showVersionsDialog(items)
    }

    override fun onStatusAction(type: StatusAction) {
        val info = details?.info ?: return
        val appId = appId ?: return
        when (type) {
            StatusAction.EDIT_META -> router?.openEditMetaScreen(
                appId,
                info.label,
                info.icon,
                info.packageName,
                info.sha1
            )

            StatusAction.UNPUBLISH -> router?.openUnpublishScreen(
                appId, details?.info?.label
            )

            else -> Unit
        }
    }

    override fun onScreenshotClick(items: List<ScreenshotItem>, clicked: Int) {
        router?.openGallery(
            items = items.map { GalleryItem(it.original, it.width, it.height) },
            current = clicked,
        )
    }

}

private const val KEY_DETAILS = "details"
private const val KEY_INSTALLED_VERSION = "versionCode"
private const val KEY_DOWNLOAD_STATE = "downloadState"
private const val KEY_TARGET_FILE = "targetFile"
private const val KEY_NEED_INSTALL = "needInstall"
private const val KEY_IS_FAVORITE = "isFavorite"
private const val KEY_TRANSLATION_DATA = "translation"
private const val KEY_TRANSLATION_STATE = "translationState"
