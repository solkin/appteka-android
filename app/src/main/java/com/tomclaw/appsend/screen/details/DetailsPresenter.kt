package com.tomclaw.appsend.screen.details

import android.os.Build
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.delivery.AppDelivery
import com.tomclaw.appsend.delivery.DeliveryState
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProvider
import com.tomclaw.appsend.screen.details.adapter.ItemListener
import com.tomclaw.appsend.screen.details.adapter.play.PlaySecurityStatus
import com.tomclaw.appsend.screen.details.adapter.scores.ScoresItem
import com.tomclaw.appsend.screen.details.adapter.screenshot.ScreenshotItem
import com.tomclaw.appsend.screen.details.adapter.status.StatusAction
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.screen.details.api.AIReview
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_BUILD
import com.tomclaw.appsend.screen.details.api.RATING_SCOPE_EDITION
import com.tomclaw.appsend.screen.details.api.RatingEntity
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_COMPLETED
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_MALWARE
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_SUSPICIOUS
import com.tomclaw.appsend.screen.details.api.TranslationResponse
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterCapabilityErrors
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.isHttpError
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.api.BreadcrumbCategory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.kotlin.plusAssign
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

        fun openRatingsScreen(
            appId: String,
            hasBuildScope: Boolean = false,
            initialScope: String = RATING_SCOPE_EDITION,
        )

        fun openProfile(userId: Int)

        fun launchApp(packageName: String)

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
            sha1: String,
            size: Long,
        )

        fun openUnpublishScreen(appId: String, label: String?)

        fun openUnlinkScreen(appId: String, label: String?)

        fun openAbuseScreen(url: String, label: String?, text: String)

        fun openDetailsScreen(appId: String, label: String?)

        fun openDetailsScreenByRef(ref: String, label: String?)

        fun openChatScreen(topicId: Int, label: String?)

        fun openStoreScreen()

        fun openTagScreen(tag: String)

        fun openGooglePlay(packageName: String)

        fun startDownload(
            label: String,
            version: String,
            icon: String?,
            appId: String,
            url: String,
            sha1: String?,
        )

        fun openShare(title: String, text: String)

        fun openLoginScreen()

        fun openGallery(items: List<GalleryItem>, current: Int)

    }

}

class DetailsPresenterImpl(
    private val appId: String?,
    private val packageName: String?,
    private val ref: String?,
    private val moderation: Boolean,
    private val finishOnly: Boolean,
    private var openReview: Boolean,
    private val bananalytics: Bananalytics,
    private val interactor: DetailsInteractor,
    private val resourceProvider: DetailsResourceProvider,
    private val abiResourceProvider: AbiResourceProvider,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val detailsConverter: DetailsConverter,
    private val packageObserver: PackageObserver,
    private val installedAppProvider: InstalledAppProvider,
    private val appDelivery: AppDelivery,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DetailsPresenter {

    private var view: DetailsView? = null
    private var router: DetailsPresenter.DetailsRouter? = null

    private var details: Details? = state?.getParcelableCompat(KEY_DETAILS, Details::class.java)

    // The build on screen: opened by package or release, the screen learns it
    // only from the loaded details.
    private val shownAppId: String?
        get() = details?.info?.appId ?: appId
    private var installed: InstalledApp =
        state?.getParcelableCompat(KEY_INSTALLED, InstalledApp::class.java) ?: InstalledApp.NONE
    private var delivery: DeliveryState =
        state?.getParcelableCompat(KEY_DELIVERY, DeliveryState::class.java) ?: DeliveryState.Idle
    private var removeBeforeInstall: Boolean = state?.getBoolean(KEY_REMOVE_BEFORE_INSTALL) == true
    private var isFavorite: Boolean = state?.getBoolean(KEY_IS_FAVORITE) == true
    private var translationData: TranslationResponse? =
        state?.getParcelableCompat(KEY_TRANSLATION_DATA, TranslationResponse::class.java)
    private var translationState: Int = state?.getInt(KEY_TRANSLATION_STATE) ?: TRANSLATION_ORIGINAL
    private var reviewScope: String =
        state?.getString(KEY_REVIEW_SCOPE) ?: RATING_SCOPE_EDITION
    private var buildRatings: ArrayList<RatingEntity>? =
        state?.getParcelableArrayListCompat(KEY_BUILD_RATINGS, RatingEntity::class.java)

    private var aiReview: AIReview? = null

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val observerSubscription = CompositeDisposable()
    private val buildRatingsDisposable = SerialDisposable()

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
            shownAppId?.let { appId ->
                val info = details?.info ?: return@subscribe
                router?.openEditMetaScreen(
                    appId = appId,
                    label = info.label,
                    icon = info.icon,
                    packageName = info.packageName,
                    sha1 = info.sha1,
                    size = info.size,
                )
            }
        }
        subscriptions += view.unpublishClicks().subscribe {
            shownAppId?.let { appId -> router?.openUnpublishScreen(appId, details?.info?.label) }
        }
        subscriptions += view.unlinkClicks().subscribe {
            shownAppId?.let { appId -> router?.openUnlinkScreen(appId, details?.info?.label) }
        }
        subscriptions += view.deleteClicks().subscribe { isConfirmed ->
            if (!isConfirmed) {
                view.showDeletionDialog()
            } else {
                deleteFromStore()
            }
        }
        subscriptions += view.abuseClicks().subscribe {
            val url = details?.url ?: return@subscribe
            val info = details?.info ?: return@subscribe
            val label = info.label
            val text = resourceProvider.formatAbuseText(
                label = info.label,
                packageName = info.packageName,
                version = info.version,
                versionCode = info.versionCode,
                size = info.size,
                url = url,
            )
            router?.openAbuseScreen(url, label, text)
        }
        subscriptions += view.versionClicks().subscribe { version ->
            details?.let { details ->
                router?.leaveScreen()
                router?.openDetailsScreen(version.appId, details.info.label)
            }
        }
        // If the moderator's decision matches the AI verdict, defer to
        // the server-side apply endpoint — it reuses AI's reason/comment
        // for declines so we skip the picker. Otherwise fall through to
        // the regular flow.
        subscriptions += view.moderationClicks().subscribe { isApprove ->
            val aiDecision = aiReview?.decision
            val aiAgrees = (isApprove && aiDecision == 1) || (!isApprove && aiDecision == -1)
            when {
                aiAgrees -> applyAIReview()
                isApprove -> sendModerationDecision(isApprove = true)
                else -> loadRejectionReasonsThenShowDialog()
            }
        }
        // Two-step decline flow is owned by the presenter: the view
        // only reports "the moderator picked a reason". If the reason
        // requires a comment we ask the view to surface the comment
        // dialog; otherwise we submit straight away.
        subscriptions += view.reasonPicked().subscribe { reason ->
            if (reason.requiresComment) {
                view.showDeclineCommentDialog(reason)
            } else {
                sendModerationDecision(
                    isApprove = false,
                    reasonCode = reason.code,
                    reasonComment = null,
                )
            }
        }
        subscriptions += view.declineConfirmed().subscribe { submission ->
            sendModerationDecision(
                isApprove = false,
                reasonCode = submission.reasonCode,
                reasonComment = submission.reasonComment,
            )
        }
        subscriptions += view.retryClicks().subscribe { invalidateDetails() }
        subscriptions += view.favoriteClicks().subscribe { isFavorite ->
            markFavorite(isFavorite)
        }
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }
        subscriptions += view.securityDownloadConfirmClicks().subscribe {
            router?.requestStoragePermissions { onInstallBypassingAbiCheck() }
        }
        subscriptions += view.abiDownloadConfirmClicks().subscribe {
            router?.requestStoragePermissions { onInstall() }
        }
        subscriptions += view.installExistingClicks().subscribe {
            onInstallExistingClick()
        }
        subscriptions += view.replaceConfirmClicks().subscribe { removeFirst ->
            onReplaceConfirm(removeFirst)
        }

        if (moderation) {
            view.showModeration()
            loadAIReviewForModerator()
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
        buildRatingsDisposable.set(null)
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
        putParcelable(KEY_INSTALLED, installed)
        putParcelable(KEY_DELIVERY, delivery)
        putBoolean(KEY_REMOVE_BEFORE_INSTALL, removeBeforeInstall)
        putBoolean(KEY_IS_FAVORITE, isFavorite)
        putParcelable(KEY_TRANSLATION_DATA, translationData)
        putInt(KEY_TRANSLATION_STATE, translationState)
        putString(KEY_REVIEW_SCOPE, reviewScope)
        putParcelableArrayList(KEY_BUILD_RATINGS, buildRatings)
    }

    private fun loadDetails(silent: Boolean = false) {
        // Once a build is on screen, reloads keep to it (the server reads
        // app_id first): after an action on it, its package or release could
        // resolve to another variant.
        subscriptions += interactor.loadDetails(shownAppId, packageName, ref)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                if (!silent) {
                    view?.hideMenu()
                    view?.hideError()
                    view?.hideNotFound()
                    view?.showProgress()
                }
            }
            .subscribe(
                { onDetailsLoaded(it) },
                { onLoadingError(it) }
            )
    }

    private fun onDetailsLoaded(details: Details) {
        this.details = details
        this.isFavorite = details.isFavorite == true
        this.buildRatings = null
        this.buildRatingsDisposable.set(null)
        if (details.buildStats == null) {
            this.reviewScope = RATING_SCOPE_EDITION
        } else if (this.reviewScope == RATING_SCOPE_BUILD && details.buildStats.rateCount > 0) {
            loadBuildRatings(details.info.appId)
        }
        details.translation?.let { translation ->
            val hasContent = !translation.description.isNullOrBlank() ||
                !translation.whatsNew.isNullOrBlank() ||
                !translation.aiNote.isNullOrBlank()
            if (hasContent) {
                this.translationData = translation
                this.translationState = TRANSLATION_TRANSLATED
            }
        }
        dispatchPackageStatus()
        ensureAIPolling()
    }

    private fun dispatchPackageStatus() {
        val packageName = details?.info?.packageName ?: return
        val appId = details?.info?.appId ?: return
        observerSubscription.clear()
        observerSubscription += packageObserver.observe(packageName)
            // Package events arrive on the main thread; signatures are read off it.
            .observeOn(schedulers.io())
            // Read on every event: a reinstall may bring another key.
            .map { versionCode -> installedAppProvider.installedApp(packageName, versionCode) }
            // switchMap, not flatMap: every package event used to add another
            // subscription to the same download relay without dropping the
            // previous one, so the screen re-bound once per accumulated one
            .switchMap { installed ->
                appDelivery.state(appId).map { delivery -> installed to delivery }
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .subscribe(
                { (installed, delivery) ->
                    this.installed = installed
                    this.delivery = delivery
                    installAfterRemoval()
                    bindDetails()
                    view?.showContent()
                    if (openReview) {
                        openReview = false
                        onRateClick(rating = 0f, review = null)
                    }
                }, {}
            )
    }

    private fun bindDetails() {
        val details = this.details ?: return

        items.clear()
        items += detailsConverter.convert(
            details = details,
            delivery = delivery,
            installed = installed,
            moderation = moderation,
            translationData = translationData,
            translationState = translationState,
            reviewScope = reviewScope,
            buildRatings = buildRatings,
        )

        bindItems()

        bindMenu()

        view?.contentUpdated()
    }

    private fun bindMenu() {
        view?.showMenu(
            isFavorite = isFavorite,
            canEdit = isCapabilityAllowed(CapabilityAction.APP_EDIT_META),
            canUnlink = isCapabilityAllowed(CapabilityAction.APP_UNLINK),
            canUnpublish = isCapabilityAllowed(CapabilityAction.APP_UNPUBLISH),
            canDelete = isCapabilityAllowed(CapabilityAction.APP_DELETE),
            installExistingTitle = resolveInstallExistingTitle()
        )
    }

    /**
     * When the main button shows "Open" for an installed app, downloading
     * is offered as an overflow item: reinstall for the same version,
     * downgrade for a newer installed one, replace for another edition.
     * Returns null when there is nothing to offer.
     */
    private fun resolveInstallExistingTitle(): String? {
        return when (details?.installState(installed) ?: return null) {
            InstallState.NOT_INSTALLED, InstallState.OLDER -> null
            InstallState.SAME -> resourceProvider.reinstallTitle()
            InstallState.NEWER -> resourceProvider.installOldVersionTitle()
            InstallState.OTHER_EDITION -> resourceProvider.replaceTitle().takeIf { installed.removable }
        }
    }

    private fun bindItems() {
        adapterPresenter.get().onDataSourceChanged(items)
    }

    /** A downgrade or another edition installs only once the app is gone. */
    private fun installAfterRemoval() {
        if (removeBeforeInstall && !installed.isInstalled) {
            removeBeforeInstall = false
            onInstallClick()
        }
    }

    /**
     * Resolve a per-app capability for the currently loaded details.
     * On a missing capability map (old server / not loaded yet) we
     * return `false` — better to hide an affordance and have the
     * server reject if the user somehow triggers it, than to surface
     * a button we cannot reason about.
     */
    private fun isCapabilityAllowed(action: String): Boolean {
        return CapabilityPolicy.isAllowed(
            action = action,
            capabilities = details?.capabilities,
            allowOnUnknown = false,
        )
    }

    private fun sendModerationDecision(
        isApprove: Boolean,
        reasonCode: Int? = null,
        reasonComment: String? = null,
    ) {
        val details = details ?: return
        subscriptions += interactor.sendModerationDecision(
            appId = details.info.appId,
            isApprove = isApprove,
            reasonCode = reasonCode,
            reasonComment = reasonComment,
        )
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.showProgress()
            }
            .subscribe(
                { onModerationDecisionSent() },
                { onActionError(it) }
            )
    }

    // Decline flow: fetch the active reasons catalog and surface a
    // picker. The subscription doesn't manage progress visibility — the
    // reasons request is fast and showing a blocking spinner here
    // would feel jarring; instead errors fall back to a snackbar and
    // the dialog only opens on success.
    private fun loadAIReviewForModerator() {
        val targetAppId = shownAppId ?: return
        subscriptions += interactor.loadAIReview(targetAppId)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { review ->
                    aiReview = review
                    view?.showAIReview(
                        decision = review.decision,
                        title = resourceProvider.aiReviewTitle(review.decision, review.confidence),
                        reasonText = review.reasonText,
                    )
                },
                { /* silent — AI review is advisory */ }
            )
    }

    private fun applyAIReview() {
        val targetAppId = shownAppId ?: return
        subscriptions += interactor.applyAIReview(targetAppId)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.showProgress()
            }
            .subscribe(
                { onModerationDecisionSent() },
                { onActionError(it) }
            )
    }

    private fun loadRejectionReasonsThenShowDialog() {
        subscriptions += interactor.loadRejectionReasons()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { reasons -> view?.showDeclineReasonDialog(reasons) },
                { view?.showSnackbar(resourceProvider.rejectionReasonsLoadFailed()) }
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
        bananalytics.leaveBreadcrumb("Delete from store", BreadcrumbCategory.USER_ACTION)
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
                { onActionError(it) }
            )
    }

    private fun onApplicationDeletedFromStore() {
        router?.leaveScreen()
    }

    private fun onLoadingError(ex: Throwable) {
        bananalytics.leaveBreadcrumb("Loading error: ${ex.message}", BreadcrumbCategory.ERROR)
        // 404 on app/info means the requested app/package is simply
        // not in the Appteka catalog (e.g. user picked "Find on
        // Appteka" for an installed app that was never published).
        // It's a normal end-state, not a bug — we surface a friendly
        // empty view and skip the crash report so the dashboard stays
        // signal-only.
        if (ex.isHttpError(404)) {
            view?.hideMenu()
            view?.showContent()
            view?.showNotFound()
            return
        }
        // Sign-in prompts are a normal state, only the rest is worth a report
        ex.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
            bananalytics.trackException(ex, mapOf("screen" to "details", "appId" to appId.orEmpty()))
            view?.hideMenu()
            view?.showContent()
            view?.showError()
        }
    }

    private fun onActionError(ex: Throwable) {
        bananalytics.leaveBreadcrumb("Action error: ${ex.message}", BreadcrumbCategory.ERROR)
        ex.filterCapabilityErrors(
            authError = { view?.showUnauthorizedError() },
            capabilityDenied = { cap ->
                view?.showContent()
                bindDetails()
                view?.showCapabilityDenied(cap)
            },
            other = {
                bananalytics.trackException(ex, mapOf("screen" to "details", "appId" to appId.orEmpty()))
                view?.hideMenu()
                view?.showContent()
                view?.showError()
            },
        )
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
        val details = details ?: return
        router?.openRatingsScreen(
            appId = details.info.appId,
            hasBuildScope = details.buildStats != null,
            initialScope = reviewScope,
        )
    }

    override fun onReviewScopeSelected(scope: String) {
        val details = details ?: return
        if (details.buildStats == null || scope == reviewScope) return
        reviewScope = scope
        if (scope == RATING_SCOPE_BUILD && details.buildStats.rateCount > 0 && buildRatings == null) {
            loadBuildRatings(details.info.appId)
        }
        bindDetails()
    }

    private fun loadBuildRatings(appId: String) {
        buildRatingsDisposable.set(
            interactor.loadBuildRatings(appId)
                .observeOn(schedulers.mainThread())
                .subscribe(
                    { ratings ->
                        buildRatings = ArrayList(ratings)
                        if (reviewScope == RATING_SCOPE_BUILD) {
                            bindDetails()
                        }
                    },
                    { /* keep summary visible on transient network error */ }
                )
        )
    }

    override fun onPlayRatingClick() {
        val position = items.indexOfFirst { it is ScoresItem }
        if (position >= 0) {
            view?.scrollToPosition(position)
        } else {
            // No breakdown to scroll to — the reviews themselves are the
            // next best answer to "where does this score come from".
            onScoresClick()
        }
    }

    override fun onPlayDownloadsClick() {
        val details = details ?: return
        val buildStats = details.buildStats ?: return
        view?.showDownloadsInfoDialog(
            editionDownloads = details.info.downloads ?: 0,
            buildDownloads = buildStats.downloads,
            versionName = details.info.version,
        )
    }

    override fun onInstallClick() {
        if (delivery == DeliveryState.AwaitingConfirmation) {
            details?.info?.appId?.let { appDelivery.confirm(it) }
            return
        }
        bananalytics.leaveBreadcrumb("Install clicked", BreadcrumbCategory.USER_ACTION)
        val security = details?.security
        if (security?.status == SECURITY_STATUS_COMPLETED) {
            when (security.verdict) {
                SECURITY_VERDICT_MALWARE -> {
                    view?.showSecurityWarningDialog(
                        title = resourceProvider.securityWarningMalwareTitle(),
                        message = resourceProvider.securityWarningMalwareMessage(),
                        downloadButton = resourceProvider.securityWarningDownloadAnyway()
                    )
                    return
                }
                SECURITY_VERDICT_SUSPICIOUS -> {
                    view?.showSecurityWarningDialog(
                        title = resourceProvider.securityWarningSuspiciousTitle(),
                        message = resourceProvider.securityWarningSuspiciousMessage(),
                        downloadButton = resourceProvider.securityWarningDownloadAnyway()
                    )
                    return
                }
            }
        }
        router?.requestStoragePermissions { onInstallBypassingAbiCheck() }
    }

    private fun onInstallExistingClick() {
        when (details?.installState(installed) ?: return) {
            InstallState.NEWER -> view?.showReplaceDialog(
                title = resourceProvider.downgradeWarningTitle(),
                message = resourceProvider.downgradeWarningMessage(),
                removeButton = resourceProvider.downgradeRemoveAndInstall(),
                installOverButton = resourceProvider.downgradeInstallOver()
            )

            // Android refuses another key over the installed app.
            InstallState.OTHER_EDITION -> if (installed.removable) view?.showReplaceDialog(
                title = resourceProvider.replaceWarningTitle(),
                message = resourceProvider.replaceWarningMessage(),
                removeButton = resourceProvider.downgradeRemoveAndInstall(),
                installOverButton = null
            )

            else -> onInstallClick()
        }
    }

    private fun onReplaceConfirm(removeFirst: Boolean) {
        if (removeFirst) {
            val packageName = details?.info?.packageName ?: return
            removeBeforeInstall = true
            router?.removeApp(packageName)
        } else {
            onInstallClick()
        }
    }

    private fun onInstallBypassingAbiCheck() {
        val abiList = details?.info?.abi
        if (!abiList.isNullOrEmpty() && !abiResourceProvider.checkCompatibility(abiList)) {
            view?.showAbiWarningDialog(
                title = resourceProvider.abiWarningTitle(),
                message = resourceProvider.abiWarningMessage(),
                downloadButton = resourceProvider.securityWarningDownloadAnyway()
            )
            return
        }
        onInstall()
    }

    private fun onInstall() {
        val details = details ?: return
        router?.startDownload(
            label = details.info.label.orEmpty(),
            version = details.info.version,
            appId = details.info.appId,
            icon = details.info.icon,
            url = details.link,
            sha1 = details.info.sha1,
        )
    }

    override fun onLaunchClick(packageName: String) {
        bananalytics.leaveBreadcrumb("Launch app: $packageName", BreadcrumbCategory.USER_ACTION)
        router?.launchApp(packageName)
    }

    override fun onRemoveClick(packageName: String) {
        bananalytics.leaveBreadcrumb("Remove app: $packageName", BreadcrumbCategory.USER_ACTION)
        router?.removeApp(packageName)
    }

    override fun onCancelClick(appId: String) {
        appDelivery.cancel(appId)
    }

    override fun onDiscussClick() {
        bananalytics.leaveBreadcrumb("Open discussion", BreadcrumbCategory.USER_ACTION)
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
                        it.filterCapabilityErrors(
                            authError = { view?.showUnauthorizedError() },
                            capabilityDenied = { cap -> view?.showCapabilityDenied(cap) },
                            other = { showSnackbar(resourceProvider.createTopicError()) },
                        )
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
        val appId = shownAppId ?: return
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
        bananalytics.leaveBreadcrumb("Rate app", BreadcrumbCategory.USER_ACTION)
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
        val details = details ?: return
        val entries = details.versionsByEdition(installed).flatMap { group ->
            val header = group.edition?.let { edition ->
                EditionHeader(
                    title = resourceProvider.editionTitle(edition),
                    summary = resourceProvider.editionSummary(edition),
                    installed = group.installed,
                )
            }
            listOfNotNull(header) + group.versions.map { mark ->
                VersionItem(
                    versionId = mark.version.appId.hashCode(),
                    appId = mark.version.appId,
                    title = resourceProvider.formatVersion(mark.version),
                    compatible = mark.version.sdkVersion <= Build.VERSION.SDK_INT,
                    newer = mark.newer,
                )
            }
        }
        view?.showVersionsDialog(entries)
    }

    override fun onStatusAction(type: StatusAction) {
        val info = details?.info ?: return
        val appId = shownAppId ?: return
        when (type) {
            StatusAction.EDIT_META -> router?.openEditMetaScreen(
                appId = appId,
                label = info.label,
                icon = info.icon,
                packageName = info.packageName,
                sha1 = info.sha1,
                size = info.size,
            )

            StatusAction.UNPUBLISH -> router?.openUnpublishScreen(
                appId, details?.info?.label
            )

            StatusAction.OPEN_OWN_EDITION -> {
                val ref = details?.editionOf(installed.editionKey)?.ref ?: return
                router?.leaveScreen()
                router?.openDetailsScreenByRef(ref, info.label)
            }

            else -> Unit
        }
    }

    override fun onScreenshotClick(items: List<ScreenshotItem>, clicked: Int) {
        router?.openGallery(
            items = items.map { GalleryItem(it.original, it.width, it.height) },
            current = clicked,
        )
    }

    override fun onRequestSecurityScan(appId: String) {
        subscriptions += interactor.requestSecurityScan(appId)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onSecurityScanRequested() },
                { onSecurityScanError(it) }
            )
    }

    override fun onSecurityInfoClick(status: PlaySecurityStatus, score: Int?) {
        view?.showSecurityInfoDialog(status, score)
    }

    private fun onSecurityScanRequested() {
        view?.showSnackbar(resourceProvider.securityScanRequestedText())
        invalidateDetails()
    }

    private fun onSecurityScanError(ex: Throwable) {
        ex.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
            view?.showSnackbar(resourceProvider.securityScanErrorText())
        }
    }

    override fun onRequestAIReview(appId: String) {
        subscriptions += interactor.requestAIReview(appId)
            .toObservable()
            .observeOn(schedulers.mainThread())
            .subscribe(
                {
                    // Silent reload so the block flips to PENDING and
                    // the poller picks up. invalidateDetails() would
                    // briefly show the fullscreen progress; the AI
                    // block itself is the progress indicator.
                    loadDetails(silent = true)
                },
                { onAIReviewError(it) }
            )
    }

    private fun onAIReviewError(ex: Throwable) {
        ex.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
            view?.showSnackbar(resourceProvider.aiReviewErrorText())
        }
    }

    override fun onTagClick(tag: String) {
        router?.openTagScreen(tag)
    }

    override fun onSimilarAppClick(appId: String, title: String) {
        router?.openDetailsScreen(appId, title)
    }

    private var aiPollingDisposable: Disposable? = null
    private var aiPollStartedAt: Long = 0L

    private fun ensureAIPolling() {
        val meta = details?.meta ?: return
        if (meta.aiStatus != AI_STATUS_PENDING) {
            stopAIPolling()
            return
        }
        if (aiPollingDisposable?.isDisposed == false) {
            return
        }
        if (aiPollStartedAt == 0L) {
            aiPollStartedAt = System.currentTimeMillis()
        }
        if (System.currentTimeMillis() - aiPollStartedAt > AI_POLL_TIMEOUT_MS) {
            stopAIPolling()
            return
        }
        val disposable = Observable.timer(AI_POLL_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .observeOn(schedulers.mainThread())
            .subscribe({ loadDetails(silent = true) }, {})
        aiPollingDisposable = disposable
        subscriptions += disposable
    }

    private fun stopAIPolling() {
        aiPollingDisposable?.dispose()
        aiPollingDisposable = null
        aiPollStartedAt = 0L
    }

}

private const val AI_STATUS_PENDING = "pending"
private const val AI_POLL_INTERVAL_MS = 3000L
private const val AI_POLL_TIMEOUT_MS = 60_000L

private const val KEY_DETAILS = "details"
private const val KEY_INSTALLED = "installed"
private const val KEY_DELIVERY = "delivery"
private const val KEY_REMOVE_BEFORE_INSTALL = "removeBeforeInstall"
private const val KEY_IS_FAVORITE = "isFavorite"
private const val KEY_TRANSLATION_DATA = "translation"
private const val KEY_TRANSLATION_STATE = "translationState"
private const val KEY_REVIEW_SCOPE = "reviewScope"
private const val KEY_BUILD_RATINGS = "buildRatings"
