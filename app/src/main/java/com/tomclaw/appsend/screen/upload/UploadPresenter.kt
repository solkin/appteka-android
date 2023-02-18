package com.tomclaw.appsend.screen.upload

import android.content.pm.PackageInfo
import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.upload.adapter.ItemListener
import com.tomclaw.appsend.screen.upload.adapter.select_app.SelectAppItem
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface UploadPresenter : ItemListener {

    fun attachView(view: UploadView)

    fun detachView()

    fun attachRouter(router: UploadRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onAppSelected(packageInfo: PackageInfo)

    fun onBackPressed()

    interface UploadRouter {

        fun openSelectAppScreen()

        fun leaveScreen()

    }

}

class UploadPresenterImpl(
    private val startInfo: PackageInfo?,
    private val interactor: UploadInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UploadPresenter {

    private var view: UploadView? = null
    private var router: UploadPresenter.UploadRouter? = null

    private var packageInfo: PackageInfo? = state?.getParcelable(KEY_PACKAGE_INFO) ?: startInfo

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: UploadView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe { onInvalidate() }

        bindUploadInfo()
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: UploadPresenter.UploadRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelable(KEY_PACKAGE_INFO, packageInfo)
    }

    override fun onAppSelected(packageInfo: PackageInfo) {
        this.packageInfo = packageInfo
        checkAppUploaded()
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun checkAppUploaded() {
        // use check API
        bindUploadInfo()
    }

    private fun bindUploadInfo() {
        var id: Long = 1

        items.clear()
        if (packageInfo == null) {
            items += SelectAppItem(id++)
        }

        bindItems()

        view?.contentUpdated()
    }

    private fun bindItems() {
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
    }

    private fun onInvalidate() {

    }

    override fun onSelectAppClick() {
        router?.openSelectAppScreen()
    }

}

private const val KEY_PACKAGE_INFO = "package_info"
