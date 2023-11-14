package com.tomclaw.appsend.screen.upload

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.screen.upload.adapter.ItemListener
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadInfo
import com.tomclaw.appsend.upload.UploadManager
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.upload.UploadStatus
import com.tomclaw.appsend.util.PackageIconLoader
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

interface UploadPresenter : ItemListener {

    fun attachView(view: UploadView)

    fun detachView()

    fun attachRouter(router: UploadRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onAppSelected(pkg: UploadPackage, apk: UploadApk)

    fun onAuthorized()

    fun onBackPressed()

    interface UploadRouter {

        fun openSelectAppScreen()

        fun openDetailsScreen(appId: String, label: String?, isFinish: Boolean)

        fun startUpload(pkg: UploadPackage, apk: UploadApk?, info: UploadInfo)

        fun openLoginScreen()

        fun leaveScreen()

        fun hideKeyboard()

    }

}

class UploadPresenterImpl(
    startPackage: UploadPackage?,
    startApkInfo: UploadApk?,
    startInfo: UploadInfo?,
    private val interactor: UploadInteractor,
    private val categoriesInteractor: CategoriesInteractor,
    private val categoryConverter: CategoryConverter,
    private val uploadConverter: UploadConverter,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val uploadManager: UploadManager,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UploadPresenter {

    private var view: UploadView? = null
    private var router: UploadPresenter.UploadRouter? = null

    private var pkg: UploadPackage? =
        state?.getParcelableCompat(KEY_PACKAGE_INFO, UploadPackage::class.java)
            ?: startPackage
    private var apk: UploadApk? =
        state?.getParcelableCompat(KEY_APK_INFO, UploadApk::class.java)
            ?: startApkInfo
    private var checkExist: CheckExistResponse? =
        state?.getParcelableCompat(KEY_CHECK_EXIST, CheckExistResponse::class.java)
            ?: startInfo?.checkExist
    private var category: CategoryItem? =
        state?.getParcelableCompat(KEY_CATEGORY_ID, CategoryItem::class.java) ?: startInfo?.category
    private var whatsNew: String = state?.getString(KEY_WHATS_NEW) ?: startInfo?.whatsNew.orEmpty()
    private var description: String =
        state?.getString(KEY_DESCRIPTION) ?: startInfo?.description.orEmpty()
    private var exclusive: Boolean =
        state?.getBoolean(KEY_EXCLUSIVE) ?: startInfo?.exclusive ?: false
    private var openSource: Boolean =
        state?.getBoolean(KEY_OPEN_SOURCE) ?: startInfo?.openSource ?: false
    private var sourceUrl: String =
        state?.getString(KEY_SOURCE_URL) ?: startInfo?.sourceUrl.orEmpty()
    private var highlightErrors: Boolean = state?.getBoolean(KEY_HIGHLIGHT_ERRORS) ?: false

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val statusSubscription = CompositeDisposable()

    override fun attachView(view: UploadView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe {
            view.showContent()
            view.hideError()
            invalidate()
        }
        subscriptions += view.categorySelectedClicks().subscribe { categoryItem ->
            onCategorySelected(categoryItem)
        }
        subscriptions += view.categoryClearedClicks().subscribe {
            onCategorySelected()
        }
        subscriptions += view.versionClicks().subscribe { versionItem ->
            router?.openDetailsScreen(
                appId = versionItem.appId,
                label = versionItem.title,
                isFinish = false
            )
        }
        subscriptions += view.cancelClicks().subscribe {
            pkg?.uniqueId?.let { uniqueId ->
                uploadManager.cancel(uniqueId)
            }
        }
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }

        invalidate()
    }

    override fun detachView() {
        subscriptions.clear()
        statusSubscription.clear()
        this.view = null
    }

    override fun attachRouter(router: UploadPresenter.UploadRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelable(KEY_PACKAGE_INFO, pkg)
        putParcelable(KEY_APK_INFO, apk)
        putParcelable(KEY_CHECK_EXIST, checkExist)
        putParcelable(KEY_CATEGORY_ID, category)
        putString(KEY_WHATS_NEW, whatsNew)
        putString(KEY_DESCRIPTION, description)
        putBoolean(KEY_EXCLUSIVE, exclusive)
        putBoolean(KEY_OPEN_SOURCE, openSource)
        putString(KEY_SOURCE_URL, sourceUrl)
        putBoolean(KEY_HIGHLIGHT_ERRORS, highlightErrors)
    }

    override fun onAppSelected(pkg: UploadPackage, apk: UploadApk) {
        onPackageChanged(pkg, apk)
    }

    override fun onAuthorized() {
        invalidate()
    }

    override fun onDiscardClick() {
        onPackageChanged(pkg = null, apk = null)
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun checkAppUploaded() {
        val pkg = pkg ?: return
        val apk = apk

        val sha1Observer = if (pkg.sha1 != null) {
            Single.create { it.onSuccess(pkg.sha1) }.toObservable()
        } else if (apk != null) {
            interactor.calculateSha1(apk.path)
        } else {
            return
        }

        subscriptions += sha1Observer
            .flatMap { interactor.checkExist(it, pkg.packageName) }
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideError()
                view?.showProgress()
            }
            .subscribe(
                { onCheckExistLoaded(it) },
                {
                    it.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
                        onCheckExistError()
                    }
                }
            )
    }

    private fun onCheckExistLoaded(response: CheckExistResponse) {
        this.checkExist = response
        response.meta?.let { meta ->
            this.category = meta.category?.let { categoryConverter.convert(it) } ?: this.category
            this.whatsNew = meta.whatsNew.orEmpty()
            this.description = meta.description.orEmpty()
            this.exclusive = meta.exclusive ?: false
            this.openSource = meta.openSource ?: false
            this.sourceUrl = meta.sourceUrl.orEmpty()
        }
        view?.showContent()
        bindForm()
    }

    private fun onCheckExistError() {
        view?.showContent()
        view?.showError()
    }

    private fun bindForm() {
        items.clear()
        items += uploadConverter.convert(
            pkg,
            apk,
            checkExist,
            category,
            whatsNew,
            description,
            exclusive,
            openSource,
            sourceUrl,
            highlightErrors,
        )

        bindItems()

        view?.contentUpdated()
    }

    private fun bindItems() {
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
    }

    private fun bindUploadAppIcon() {
        apk?.packageInfo?.let { packageInfo ->
            val uri = PackageIconLoader.getUri(packageInfo)
            view?.setAppIcon(uri)
        }
    }

    private fun onPackageChanged(pkg: UploadPackage?, apk: UploadApk?) {
        val thisPkg = this.pkg
        val nextPkg = pkg
        if (nextPkg == null || (thisPkg != null && thisPkg.packageName != nextPkg.packageName)) {
            clearForm()
        }
        this.pkg = pkg
        this.apk = apk
        this.checkExist = null
        invalidate()
    }

    private fun invalidate() {
        if (checkExist == null && pkg != null) {
            checkAppUploaded()
        } else {
            bindForm()
        }
        subscribeStatusChange(pkg)
    }

    private fun clearForm() {
        category = null
        whatsNew = ""
        description = ""
        exclusive = false
        openSource = false
        sourceUrl = ""
        highlightErrors = false
        bindForm()
    }

    private fun subscribeStatusChange(pkg: UploadPackage?) {
        statusSubscription.clear()
        pkg ?: return
        bindUploadAppIcon()
        statusSubscription += uploadManager.status(id = pkg.uniqueId)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .subscribe({ state ->
                when (state.status) {
                    UploadStatus.IDLE -> view?.showContent()
                    UploadStatus.COMPLETED -> {
                        if (state.result?.appId != null) {
                            router?.openDetailsScreen(
                                appId = state.result.appId,
                                label = checkExist?.file?.title.orEmpty(),
                                isFinish = true
                            )
                        } else {
                            view?.showError()
                        }
                    }

                    UploadStatus.AWAIT -> {
                        view?.resetUploadProgress()
                        view?.showUploadProgress()
                    }

                    UploadStatus.ERROR -> view?.showError()
                    UploadStatus.STARTED -> {
                        view?.showUploadProgress()
                    }

                    else -> view?.setUploadProgress(state.percent)
                }
            }, {})
    }

    override fun onSelectAppClick() {
        router?.openSelectAppScreen()
    }

    override fun onNoticeClick() {
        val file = checkExist?.file ?: return
        router?.openDetailsScreen(appId = file.appId, label = file.title, isFinish = false)
    }

    override fun onCategoryClick() {
        loadCategories()
    }

    override fun onWhatsNewChanged(text: String) {
        whatsNew = text
    }

    override fun onDescriptionChanged(text: String) {
        description = text
    }

    override fun onExclusiveChanged(value: Boolean) {
        exclusive = value
    }

    override fun onOpenSourceChanged(value: Boolean, url: String) {
        openSource = value
        sourceUrl = url
    }

    override fun onSubmitClick() {
        highlightErrors = true
        val uploadStarted = startUpload()
        if (!uploadStarted) {
            bindForm()
            view?.scrollToTop()
        }
    }

    private fun startUpload(): Boolean {
        val pkg = pkg ?: return false
        val category = category ?: return false
        val checkExist = checkExist ?: return false
        description.ifBlank { null } ?: return false

        val info = UploadInfo(
            checkExist,
            category,
            description,
            whatsNew,
            exclusive,
            openSource,
            sourceUrl
        )

        router?.hideKeyboard()
        router?.startUpload(pkg, apk, info)
        return true
    }

    override fun onOtherVersionsClick(items: List<VersionItem>) {
        view?.showVersionsDialog(items)
    }

    private fun loadCategories() {
        subscriptions += categoriesInteractor.getCategories()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { onCategoriesLoaded(it) },
                { onCategoriesLoadingError() }
            )
    }

    private fun onCategoriesLoadingError() {
        view?.showError()
    }

    private fun onCategoriesLoaded(categories: List<Category>) {
        val items = categories.map { categoryConverter.convert(it) }.sortedBy { it.title }
        view?.showCategories(items)
    }

    private fun onCategorySelected(categoryItem: CategoryItem? = null) {
        category = categoryItem
        bindForm()
    }

}

private const val KEY_PACKAGE_INFO = "package_info"
private const val KEY_APK_INFO = "apk_info"
private const val KEY_CHECK_EXIST = "check_exist"
private const val KEY_CATEGORY_ID = "category"
private const val KEY_WHATS_NEW = "whats_new"
private const val KEY_DESCRIPTION = "description"
private const val KEY_EXCLUSIVE = "exclusive"
private const val KEY_OPEN_SOURCE = "open_source"
private const val KEY_SOURCE_URL = "source_url"
private const val KEY_HIGHLIGHT_ERRORS = "highlight_errors"
