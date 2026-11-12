package com.tomclaw.appsend.screen.installed

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.download.AWAIT
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.download.COMPLETED
import com.tomclaw.appsend.download.DownloadManager
import com.tomclaw.appsend.download.DownloadParams
import com.tomclaw.appsend.download.ERROR
import com.tomclaw.appsend.download.IDLE
import com.tomclaw.appsend.download.STARTED
import com.tomclaw.appsend.install.AppInstaller
import com.tomclaw.appsend.install.InstallRequest
import com.tomclaw.appsend.install.InstallStatus
import com.tomclaw.appsend.install.asDownloadState
import com.tomclaw.appsend.screen.details.adapter.abi.AbiResourceProvider
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.SECURITY_STATUS_COMPLETED
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_MALWARE
import com.tomclaw.appsend.screen.details.api.SECURITY_VERDICT_SUSPICIOUS
import com.tomclaw.appsend.screen.installed.adapter.ItemListener
import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.FileHelper.escapeFileSymbols
import com.tomclaw.appsend.util.PackageObserver
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import dagger.Lazy
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit

interface InstalledPresenter : ItemListener {

    fun attachView(view: InstalledView)

    fun detachView()

    fun attachRouter(router: InstalledRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun showSnackbar(text: String)

    fun saveFile(target: Uri)

    fun invalidateApps()

    interface InstalledRouter {

        fun openAppScreen(appId: String, title: String)

        fun startDownload(params: DownloadParams)

        fun launchApp(packageName: String)

        fun openShareApk(uri: Uri, fileName: String)

        fun openShareBluetooth(uri: Uri, fileName: String)

        fun openUploadScreen(pkg: UploadPackage, apk: UploadApk)

        fun searchGooglePlay(packageName: String)

        fun searchAppteka(packageName: String, title: String)

        fun openPermissionsScreen(permissions: List<String>)

        fun openSystemDetailsScreen(packageName: String)

        fun removeApp(packageName: String)

        fun requestSaveFile(fileName: String, fileType: String)

        fun requestStoragePermissions(callback: (Boolean) -> Unit)

        fun requestDownloadPermissions(callback: (Boolean) -> Unit)

        fun leaveScreen(path: String? = null)

    }

}

class InstalledPresenterImpl(
    private val picker: Boolean,
    private val preferencesProvider: InstalledPreferencesProvider,
    private val interactor: InstalledInteractor,
    private val apkStorage: ApkStorage,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val resourceProvider: AppsResourceProvider,
    private val downloadManager: DownloadManager,
    private val appInstaller: AppInstaller,
    private val packageObserver: PackageObserver,
    private val abiResourceProvider: AbiResourceProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : InstalledPresenter {

    private var view: InstalledView? = null
    private var router: InstalledPresenter.InstalledRouter? = null

    private val subscriptions = CompositeDisposable()
    private val updateSubscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var filter: String? = state?.getString(KEY_FILTER).takeIf { !it.isNullOrBlank() }
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var extractItem: AppItem? =
        state?.getParcelableCompat(KEY_EXTRACT_ITEM, AppItem::class.java)
    private val pendingInstalls: MutableMap<String, PendingInstall> =
        state?.getParcelableArrayListCompat(KEY_PENDING_INSTALLS, PendingInstall::class.java)
            .orEmpty()
            .associateBy { it.appId }
            .toMutableMap()
    private val installStatuses = HashMap<String, InstallStatus>()
    private val detailsRequests = HashMap<String, Disposable>()
    private val downloadsStarted = HashSet<String>()
    private val installsStarting = HashSet<String>()
    private val updateStates = HashMap<String, Int>()
    private var appsRequest: Disposable? = null
    private var displayedItems: List<AppItem> = emptyList()

    override fun attachView(view: InstalledView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.itemMenuClicks().subscribe { pair ->
            // Item ids are reissued on every reload; the package is stable.
            val app = items?.find { it.packageName == pair.second.packageName }
                ?: return@subscribe
            when (pair.first) {
                MENU_RUN -> {
                    router?.launchApp(app.packageName)
                }

                MENU_SHARE -> {
                    app.path ?: return@subscribe
                    copyAndShareApk(app, bluetooth = false)
                }

                MENU_EXTRACT -> {
                    requestExtractApk(app)
                }

                MENU_UPLOAD -> {
                    val uploadInfo = interactor.getPackageUploadInfo(app.packageName)
                    router?.openUploadScreen(uploadInfo.first, uploadInfo.second)
                }

                MENU_BLUETOOTH -> {
                    app.path ?: return@subscribe
                    copyAndShareApk(app, bluetooth = true)
                }

                MENU_FIND_ON_GP -> {
                    router?.searchGooglePlay(app.packageName)
                }

                MENU_FIND_ON_STORE -> {
                    router?.searchAppteka(app.packageName, app.title)
                }

                MENU_PERMISSIONS -> {
                    router?.openPermissionsScreen(interactor.getPackagePermissions(app.packageName))
                }

                MENU_DETAILS -> {
                    router?.openSystemDetailsScreen(app.packageName)
                }

                MENU_REMOVE -> {
                    router?.removeApp(app.packageName)
                }

                MENU_OTHER_EDITION -> {
                    val other = app.otherEdition ?: return@subscribe
                    view.showOtherEditionDialog(
                        app,
                        resourceProvider.otherEditionVersion(other.verName, other.publishTime)
                    )
                }

                MENU_OPEN_OTHER_EDITION -> {
                    val other = app.otherEdition ?: return@subscribe
                    router?.openAppScreen(other.appId, other.title)
                }
            }
        }
        subscriptions += view.searchTextChanged().subscribe { text ->
            filterApps(text)
        }
        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
        }

        if (isError) {
            onError(throwable = null)
            onReady()
        } else {
            items?.let {
                onReady()
                observeUpdates()
            } ?: loadApps()
        }
    }

    private fun filterApps(text: String) {
        this.filter = text
        onReady()
    }

    override fun detachView() {
        subscriptions.clear()
        updateSubscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: InstalledPresenter.InstalledRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putString(KEY_FILTER, filter)
        putBoolean(KEY_ERROR, isError)
        putParcelable(KEY_EXTRACT_ITEM, extractItem)
        putParcelableArrayList(KEY_PENDING_INSTALLS, ArrayList(pendingInstalls.values))
    }

    override fun invalidateApps() {
        items = null
        loadApps()
    }

    /** Reloads behind the list on screen, as after an app on the device changed. */
    private fun reloadApps() {
        loadApps(showProgress = false)
    }

    private fun loadApps(showProgress: Boolean = true) {
        // A newer load replaces the one in flight, so an older answer never
        // lands on top of it.
        appsRequest?.let { subscriptions.remove(it) }
        val request = interactor.listInstalledApps(preferencesProvider.isShowSystemApps())
            .flatMap { installed ->
                val installedMap = installed.associate { it.packageName to it.verCode }
                val certsMap = installed
                    .mapNotNull { app -> app.editionKey?.let { app.packageName to it } }
                    .toMap()
                Observables.zip(
                    Single.just(installed).toObservable(),
                    Observable.just(InstalledUpdates.EMPTY).takeIf { picker }
                        ?: interactor.getUpdates(installedMap, certsMap)
                )
            }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe {
                if (showProgress && view?.isPullRefreshing() == false) view?.showProgress()
            }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it.first, it.second) },
                { onError(it) }
            )
        appsRequest = request
        subscriptions += request
    }

    private fun onLoaded(entities: List<InstalledAppEntity>, updates: InstalledUpdates) {
        isError = false
        val updatesMap = updates.entries.associateBy { it.packageName }
        val otherEditionsMap = updates.otherEditions.associateBy { it.packageName }
        val newItems = entities
            .map {
                appConverter.convert(
                    it,
                    updatesMap[it.packageName],
                    otherEditionsMap[it.packageName]
                )
            }
            .sortedWith { lhs, rhs ->
                when (preferencesProvider.getSortOrder()) {
                    SortOrder.ASCENDING -> lhs.title.uppercase().compareTo(rhs.title.uppercase())
                    SortOrder.DESCENDING -> rhs.title.uppercase().compareTo(lhs.title.uppercase())
                    SortOrder.APP_SIZE -> rhs.size.compareTo(lhs.size)
                    SortOrder.INSTALL_TIME -> rhs.installTime.compareTo(lhs.installTime)
                    SortOrder.UPDATE_TIME -> rhs.updateTime.compareTo(lhs.updateTime)
                }
            }
            .sortedBy { it.update == null }
            .toList()
        // Rows of an update in progress keep their state until its relays answer.
        newItems.forEach { item ->
            item.update?.appId?.let { appId -> item.updateState = updateStates[appId] ?: IDLE }
        }

        this.items = newItems
        observeUpdates()
    }

    /**
     * Follows each offered update through download and install, and reloads
     * the list once the app changes on the device, so the row stops offering
     * what is already installed.
     */
    private fun observeUpdates() {
        updateSubscriptions.clear()
        items.orEmpty().forEach { item ->
            val appId = item.update?.appId ?: return@forEach
            updateSubscriptions += Observable
                .combineLatest(
                    downloadManager.status(appId),
                    appInstaller.status(appId)
                ) { downloadState, installStatus -> downloadState to installStatus }
                .observeOn(schedulers.mainThread())
                .subscribe({ (downloadState, installStatus) ->
                    onUpdateState(item, downloadState, installStatus)
                }, {})
            // Only a new version matters: the system also reports components
            // switched on and off, several times per install.
            updateSubscriptions += packageObserver.observe(item.packageName)
                .distinctUntilChanged()
                .skip(1)
                .observeOn(schedulers.mainThread())
                .subscribe({ reloadApps() }, {})
        }
    }

    private fun onUpdateState(item: AppItem, downloadState: Int, installStatus: InstallStatus) {
        val appId = item.update?.appId ?: return
        installStatuses[appId] = installStatus
        when {
            downloadState == COMPLETED -> {
                downloadsStarted.remove(appId)
                installWhenReady(item, appId)
            }
            // The notification says what failed; the row offers the update again.
            downloadState == ERROR -> {
                downloadsStarted.remove(appId)
                pendingInstalls.remove(appId)
            }
            // Back to idle after it ran: cancelled here, from the notification
            // or by the system, so nothing is left to install.
            downloadState == IDLE -> if (downloadsStarted.remove(appId)) pendingInstalls.remove(appId)

            else -> downloadsStarted.add(appId)
        }
        setUpdateState(item, updateStateOf(appId, downloadState, installStatus))
    }

    private fun updateStateOf(appId: String, downloadState: Int, installStatus: InstallStatus): Int {
        // A downloaded update about to be installed is still in progress.
        if (downloadState == COMPLETED && appId in pendingInstalls) return STARTED
        return installStatus.asDownloadState(downloadState)
    }

    private fun setUpdateState(item: AppItem, state: Int) {
        item.update?.appId?.let { updateStates[it] = state }
        if (item.updateState == state) return
        item.updateState = state
        val position = displayedItems.indexOfFirst { it.id == item.id }
        if (position >= 0) view?.contentUpdated(position)
    }

    /**
     * Installs a downloaded update the user asked for here. The storage may not
     * hand the file over at once after the download commits (MediaStore), and
     * COMPLETED is never repeated, so the lookup is retried a few times. The
     * pending entry stays until the install is handed over, so a screen torn
     * down meanwhile picks it up again once recreated.
     */
    private fun installWhenReady(item: AppItem, appId: String) {
        val pending = pendingInstalls[appId] ?: return
        if (!installsStarting.add(appId)) return
        val finish = { failed: Boolean ->
            pendingInstalls.remove(appId)
            if (failed) view?.showSnackbar(resourceProvider.updateFailed())
            val installStatus = installStatuses[appId] ?: InstallStatus.Idle
            setUpdateState(item, updateStateOf(appId, COMPLETED, installStatus))
        }
        subscriptions += Observable
            .interval(0, INSTALL_RETRY_DELAY_MS, TimeUnit.MILLISECONDS, schedulers.io())
            .take(INSTALL_RETRY_ATTEMPTS)
            .concatMapMaybe {
                Maybe.fromCallable {
                    downloadManager.getInstallUri(pending.label, pending.version, appId)
                }
            }
            .firstElement()
            .observeOn(schedulers.mainThread())
            .doFinally { installsStarting.remove(appId) }
            .subscribe(
                { uri ->
                    appInstaller.install(InstallRequest(key = appId, label = pending.label, uri = uri))
                    finish(false)
                },
                { finish(true) },
                { finish(true) }
            )
    }

    private fun onReady() {
        val items = this.items?.filter {
            it.title.contains(
                StringBuilder(filter.orEmpty()),
                ignoreCase = true
            )
        }
        displayedItems = items.orEmpty()
        when {
            isError -> {
                view?.showError()
            }

            items.isNullOrEmpty() -> {
                view?.showPlaceholder()
            }

            else -> {
                adapterPresenter.get().onDataSourceChanged(items)
                view?.let {
                    it.contentUpdated()
                    if (it.isPullRefreshing()) {
                        it.stopPullRefreshing()
                    } else {
                        it.showContent()
                    }
                }
            }
        }
    }

    private fun onError(throwable: Throwable?) {
        this.isError = true
        throwable?.printStackTrace()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun showSnackbar(text: String) {
        view?.showSnackbar(text)
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        if (picker) {
            router?.leaveScreen(app.path)
        } else {
            view?.showItemDialog(app)
        }
    }

    override fun onUpdateClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        val update = app.update ?: return
        val appId = update.appId
        setUpdateState(app, AWAIT)
        lateinit var request: Disposable
        request = interactor.getDetails(appId)
            .observeOn(schedulers.mainThread())
            .doFinally { if (detailsRequests[appId] === request) detailsRequests.remove(appId) }
            .subscribe(
                { details -> onUpdateDetails(app, details) },
                {
                    setUpdateState(app, IDLE)
                    view?.showSnackbar(resourceProvider.updateFailed())
                }
            )
        detailsRequests.put(appId, request)?.dispose()
        subscriptions += request
    }

    private fun onUpdateDetails(app: AppItem, details: Details) {
        val router = router ?: return setUpdateState(app, IDLE)
        val info = details.info
        val appId = app.update?.appId ?: return
        if (info.appId != appId) {
            // Not the build the row offers: let its page tell what it is.
            setUpdateState(app, IDLE)
            router.openAppScreen(info.appId, app.title)
            return
        }
        val verdict = details.security
            ?.takeIf { it.status == SECURITY_STATUS_COMPLETED }
            ?.verdict
        val abi = info.abi
        val isRisky = verdict == SECURITY_VERDICT_MALWARE || verdict == SECURITY_VERDICT_SUSPICIOUS
        val isIncompatible = !abi.isNullOrEmpty() && !abiResourceProvider.checkCompatibility(abi)
        if (isRisky || isIncompatible) {
            // The app page is where those warnings are shown and accepted.
            setUpdateState(app, IDLE)
            router.openAppScreen(info.appId, app.title)
            return
        }
        router.requestDownloadPermissions { granted ->
            // Cancelled while the permission was being asked.
            if (updateStates[appId] != AWAIT) return@requestDownloadPermissions
            if (!granted) {
                setUpdateState(app, IDLE)
                return@requestDownloadPermissions
            }
            val label = info.label.orEmpty()
            pendingInstalls[info.appId] = PendingInstall(info.appId, label, info.version)
            router.startDownload(
                DownloadParams(
                    label = label,
                    version = info.version,
                    icon = info.icon,
                    appId = info.appId,
                    url = details.link,
                    sha1 = info.sha1,
                )
            )
        }
    }

    override fun onUpdateCancelClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        val appId = app.update?.appId ?: return
        pendingInstalls.remove(appId)
        val request = detailsRequests.remove(appId)
        if (request != null) {
            // Still asking for the link: nothing was downloaded yet.
            request.dispose()
            setUpdateState(app, IDLE)
            return
        }
        if (installStatuses[appId] == null || installStatuses[appId] == InstallStatus.Idle) {
            downloadManager.cancel(appId)
        } else {
            appInstaller.cancel(appId)
        }
        // Waiting for a permission answer or a free download slot: nothing
        // will report the cancel back.
        if (app.updateState == AWAIT) setUpdateState(app, IDLE)
    }

    private fun requestExtractApk(app: AppItem) {
        app.path ?: return
        extractItem = app
        router?.requestSaveFile(
            fileName = fileName(
                label = app.title,
                version = app.version,
                packageName = app.packageName
            ),
            fileType = "application/vnd.android.package-archive"
        )
    }

    override fun saveFile(target: Uri) {
        val item = extractItem ?: return
        extractItem = null
        item.path ?: return
        subscriptions += interactor
            .copyFile(
                source = item.path,
                target,
            )
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { view?.showExtractSuccess() },
                { view?.showExtractError() }
            )
    }

    private fun fileName(label: String, version: String, packageName: String): String {
        return escapeFileSymbols("$label-$version-$packageName")
    }

    private fun copyAndShareApk(app: AppItem, bluetooth: Boolean) {
        val path = app.path ?: return
        if (apkStorage.isPermissionRequired()) {
            router?.requestStoragePermissions { granted ->
                if (granted) {
                    doCopyAndShareApk(path, app, bluetooth)
                }
            }
        } else {
            doCopyAndShareApk(path, app, bluetooth)
        }
    }

    private fun doCopyAndShareApk(path: String, app: AppItem, bluetooth: Boolean) {
        val fileName = fileName(app.title, app.version, app.packageName)
        val displayName = "$fileName.apk"
        subscriptions += interactor
            .copyToStorage(source = path, fileName = fileName)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { uri ->
                    if (bluetooth) {
                        router?.openShareBluetooth(uri = uri, fileName = displayName)
                    } else {
                        router?.openShareApk(uri = uri, fileName = displayName)
                    }
                },
                { view?.showExtractError() }
            )
    }

}

private const val KEY_APPS = "apps"
private const val KEY_FILTER = "filter"
private const val KEY_ERROR = "error"
private const val KEY_EXTRACT_ITEM = "extract_item"
private const val KEY_PENDING_INSTALLS = "pending_installs"
private const val INSTALL_RETRY_ATTEMPTS = 5L
private const val INSTALL_RETRY_DELAY_MS = 200L
