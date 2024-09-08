package com.tomclaw.appsend.screen.distro

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.distro.adapter.ItemListener
import com.tomclaw.appsend.screen.distro.adapter.apk.ApkItem
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
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

    interface DistroRouter {

        fun installApp(path: String)

        fun openShareApk(path: String)

        fun openShareBluetooth(path: String)

        fun openUploadScreen(pkg: UploadPackage, apk: UploadApk)

        fun searchGooglePlay(packageName: String)

        fun searchAppteka(packageName: String, title: String)

        fun openPermissionsScreen(permissions: List<String>)

        fun requestStoragePermissions(callback: () -> Unit)

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
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) ?: false

    override fun attachView(view: DistroView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe {
            onBackPressed()
        }
        subscriptions += view.itemMenuClicks().subscribe { pair ->
            val app = items?.find { it.id == pair.second.id } ?: return@subscribe
            when (pair.first) {
                MENU_INSTALL -> {
                    router?.installApp(app.path)
                }

                MENU_SHARE -> {
                    router?.openShareApk(app.path)
                }

                MENU_UPLOAD -> {
//                    val uploadInfo = interactor.getPackageUploadInfo(app.packageName)
//                    router?.openUploadScreen(uploadInfo.first, uploadInfo.second)
                }

                MENU_BLUETOOTH -> {
                    router?.openShareBluetooth(app.path)
                }

                MENU_FIND_ON_GP -> {
                    router?.searchGooglePlay(app.packageName)
                }

                MENU_FIND_ON_STORE -> {
                    router?.searchAppteka(app.packageName, app.title)
                }

                MENU_PERMISSIONS -> {
                    router?.openPermissionsScreen(interactor.getPackagePermissions(app.path))
                }

                MENU_REMOVE -> {
                    subscriptions += interactor
                        .removeApk(app.path)
                        .observeOn(schedulers.mainThread())
                        .subscribe({
                            items = items?.filter { it.path != app.path }
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

    override fun attachRouter(router: DistroPresenter.DistroRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putString(KEY_FILTER, filter)
        putBoolean(KEY_ERROR, isError)
    }

    override fun invalidateApps() {
        items = null
        loadApps()
    }

    private fun loadApps() {
        subscriptions += interactor.listDistroApps()
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .doAfterTerminate { onReady() }
            .subscribe(
                { onLoaded(it) },
                { onError(it) }
            )
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

}

private const val KEY_APPS = "apps"
private const val KEY_FILTER = "filter"
private const val KEY_ERROR = "error"
