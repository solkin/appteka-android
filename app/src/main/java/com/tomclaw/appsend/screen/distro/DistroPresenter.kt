package com.tomclaw.appsend.screen.distro

import android.net.Uri
import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.distro.adapter.ItemListener
import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItem
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.FileHelper.escapeFileSymbols
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface DistroPresenter : ItemListener {

    fun attachView(view: DistroView)

    fun detachView()

    fun attachRouter(router: DistroRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onUpdate()

    fun showSnackbar(text: String)

    fun invalidateApps()

    fun saveFile(target: Uri)

    interface DistroRouter {

        fun installApp(path: String)

        fun openShareApk(path: String)

        fun openShareBluetooth(path: String)

        fun openUploadScreen(pkg: UploadPackage, apk: UploadApk)

        fun searchGooglePlay(packageName: String)

        fun searchAppteka(packageName: String, title: String)

        fun openPermissionsScreen(permissions: List<String>)

        fun requestStoragePermissions(callback: (Boolean) -> Unit)

        fun requestSaveFile(fileName: String, fileType: String)

        fun leaveScreen()

    }

}

class DistroPresenterImpl(
    private val preferencesProvider: DistroPreferencesProvider,
    private val interactor: DistroInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: ApkConverter,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : DistroPresenter {

    private var view: DistroView? = null
    private var router: DistroPresenter.DistroRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<ApkItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, ApkItem::class.java)
    private var filter: String? = state?.getString(KEY_FILTER).takeIf { !it.isNullOrBlank() }
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var extractItem: ApkItem? =
        state?.getParcelableCompat(KEY_EXTRACT_ITEM, ApkItem::class.java)

    override fun attachView(view: DistroView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.itemMenuClicks().subscribe { pair ->
            val app = items?.find { it.id == pair.second.id } ?: return@subscribe
            when (pair.first) {
                MENU_INSTALL -> {
                    app.path?.let { router?.installApp(it) }
                }

                MENU_SHARE -> {
                    app.path?.let { router?.openShareApk(it) }
                }

                MENU_EXTRACT -> {
                    requestExtractApk(app)
                }

                MENU_UPLOAD -> {
                    interactor.getPackageUploadInfo(app.fileName)?.let { uploadInfo ->
                        router?.openUploadScreen(uploadInfo.first, uploadInfo.second)
                    }
                }

                MENU_BLUETOOTH -> {
                    app.path?.let { router?.openShareBluetooth(it) }
                }

                MENU_FIND_ON_GP -> {
                    if (app.packageName.isNotEmpty()) {
                        router?.searchGooglePlay(app.packageName)
                    }
                }

                MENU_FIND_ON_STORE -> {
                    if (app.packageName.isNotEmpty()) {
                        router?.searchAppteka(app.packageName, app.title)
                    }
                }

                MENU_PERMISSIONS -> {
                    router?.openPermissionsScreen(interactor.getPackagePermissions(app.fileName))
                }

                MENU_REMOVE -> {
                    subscriptions += interactor
                        .removeApk(app.fileName)
                        .observeOn(schedulers.mainThread())
                        .subscribe({
                            items = items?.filter { it.fileName != app.fileName }
                            onReady()
                        }, { })
                }
            }
        }
        subscriptions += view.searchTextChanged().subscribe { text ->
            filterApps(text)
        }
        subscriptions += view.shareExtractedClicks().subscribe { path ->
            router?.openShareApk(path)
        }
        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
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

    override fun attachRouter(router: DistroPresenter.DistroRouter) {
        this.router = router
        if (isError) {
            onError(throwable = null)
            onReady()
        } else {
            items?.let { onReady() } ?: loadApps()
        }
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
        router?.requestStoragePermissions { success ->
            if (success) {
                subscriptions += interactor.listDistroApps()
                    .observeOn(schedulers.mainThread())
                    .doOnSubscribe {
                        if (view?.isPullRefreshing() == false) {
                            view?.showProgress()
                        }
                    }
                    .doAfterTerminate { onReady() }
                    .subscribe(
                        { onLoaded(it) },
                        { onError(it) }
                    )
            } else {
                view?.stopPullRefreshing()
                view?.showPlaceholder()
            }
        }
    }

    private fun onLoaded(entities: List<DistroAppEntity>) {
        isError = false
        val newItems = entities
            .map { appConverter.convert(it) }
            .sortedBy { it.title }
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
        view?.showItemDialog(app)
    }

    private fun requestExtractApk(app: ApkItem) {
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
        val path = item.path ?: return
        subscriptions += interactor
            .copyFile(
                source = path,
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

}

private const val KEY_APPS = "apps"
private const val KEY_FILTER = "filter"
private const val KEY_ERROR = "error"
private const val KEY_EXTRACT_ITEM = "extract_item"
