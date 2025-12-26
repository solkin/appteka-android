package com.tomclaw.appsend.screen.installed

import android.net.Uri
import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.installed.adapter.ItemListener
import com.tomclaw.appsend.screen.installed.adapter.app.AppItem
import com.tomclaw.appsend.screen.installed.api.UpdateEntity
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.FileHelper.escapeFileSymbols
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

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

        fun launchApp(packageName: String)

        fun openShareApk(path: String)

        fun openShareBluetooth(path: String)

        fun openUploadScreen(pkg: UploadPackage, apk: UploadApk)

        fun searchGooglePlay(packageName: String)

        fun searchAppteka(packageName: String, title: String)

        fun openPermissionsScreen(permissions: List<String>)

        fun openSystemDetailsScreen(packageName: String)

        fun removeApp(packageName: String)

        fun requestSaveFile(fileName: String, fileType: String)

        fun leaveScreen(path: String? = null)

    }

}

class InstalledPresenterImpl(
    private val picker: Boolean,
    private val preferencesProvider: InstalledPreferencesProvider,
    private val interactor: InstalledInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : InstalledPresenter {

    private var view: InstalledView? = null
    private var router: InstalledPresenter.InstalledRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var filter: String? = state?.getString(KEY_FILTER).takeIf { !it.isNullOrBlank() }
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var extractItem: AppItem? =
        state?.getParcelableCompat(KEY_EXTRACT_ITEM, AppItem::class.java)

    override fun attachView(view: InstalledView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.itemMenuClicks().subscribe { pair ->
            val app = items?.find { it.id == pair.second.id } ?: return@subscribe
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
            items?.let { onReady() } ?: loadApps()
        }
    }

    private fun filterApps(text: String) {
        this.filter = text
        onReady()
    }

    override fun detachView() {
        subscriptions.clear()
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
    }

    override fun invalidateApps() {
        items = null
        loadApps()
    }

    private fun loadApps() {
        subscriptions += interactor.listInstalledApps(preferencesProvider.isShowSystemApps())
            .flatMap { installed ->
                val installedMap = installed.associate { it.packageName to it.verCode }
                Observables.zip(
                    Single.just(installed).toObservable(),
                    Observable.just(emptyList<UpdateEntity>()).takeIf { picker }
                        ?: interactor.getUpdates(installedMap)
                )
            }
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it.first, it.second) },
                { onError(it) }
            )
    }

    private fun onLoaded(entities: List<InstalledAppEntity>, updates: List<UpdateEntity>) {
        isError = false
        val updatesMap = updates.associateBy { it.packageName }
        val newItems = entities
            .map { appConverter.convert(it, updatesMap[it.packageName]) }
            .sortedWith { lhs, rhs ->
                when (preferencesProvider.getSortOrder()) {
                    SortOrder.ASCENDING -> lhs.title.uppercase().compareTo(rhs.title.uppercase())
                    SortOrder.DESCENDING -> rhs.title.uppercase().compareTo(lhs.title.uppercase())
                    SortOrder.APP_SIZE -> rhs.size.compareTo(lhs.size)
                    SortOrder.INSTALL_TIME -> rhs.installTime.compareTo(lhs.installTime)
                    SortOrder.UPDATE_TIME -> rhs.updateTime.compareTo(lhs.updateTime)
                }
            }
            .sortedBy { it.updateAppId == null }
            .toList()

        this.items = this.items
            ?.plus(newItems) ?: newItems
    }

    private fun onReady() {
        val items = this.items?.filter {
            it.title.contains(
                StringBuilder(filter.orEmpty()),
                ignoreCase = true
            )
        }
        when {
            isError -> {
                view?.showError()
            }

            items.isNullOrEmpty() -> {
                view?.showPlaceholder()
            }

            else -> {
                val dataSource = ListDataSource(items)
                adapterPresenter.get().onDataSourceChanged(dataSource)
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

    override fun onUpdateClick(title: String, appId: String) {
        router?.openAppScreen(appId, title)
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
        val fileName = fileName(app.title, app.version, app.packageName)
        subscriptions += interactor
            .copyToCache(source = path, fileName = fileName)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { cachedPath ->
                    if (bluetooth) {
                        router?.openShareBluetooth(path = cachedPath)
                    } else {
                        router?.openShareApk(path = cachedPath)
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
