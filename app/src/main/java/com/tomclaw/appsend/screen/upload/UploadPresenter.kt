package com.tomclaw.appsend.screen.upload

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.main.item.CommonItem
import com.tomclaw.appsend.screen.upload.adapter.ItemListener
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.upload.MetaInfo
import com.tomclaw.appsend.upload.UploadManager
import com.tomclaw.appsend.upload.UploadStatus
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableCompat
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
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

    fun onAppSelected(info: CommonItem)

    fun onBackPressed()

    interface UploadRouter {

        fun openSelectAppScreen()

        fun openDetailsScreen(appId: String, label: String?)

        fun startUpload(item: CommonItem, meta: MetaInfo)

        fun leaveScreen()

    }

}

class UploadPresenterImpl(
    startInfo: CommonItem?,
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

    private var packageInfo: CommonItem? =
        state?.getParcelableCompat(KEY_PACKAGE_INFO, CommonItem::class.java) ?: startInfo
    private var checkExist: CheckExistResponse? =
        state?.getParcelableCompat(KEY_CHECK_EXIST, CheckExistResponse::class.java)
    private var category: CategoryItem? =
        state?.getParcelableCompat(KEY_CATEGORY_ID, CategoryItem::class.java)
    private var whatsNew: String = state?.getString(KEY_WHATS_NEW).orEmpty()
    private var description: String = state?.getString(KEY_DESCRIPTION).orEmpty()
    private var exclusive: Boolean = state?.getBoolean(KEY_EXCLUSIVE) ?: false
    private var openSource: Boolean = state?.getBoolean(KEY_OPEN_SOURCE) ?: false
    private var sourceUrl: String = state?.getString(KEY_SOURCE_URL).orEmpty()

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()
    private val statusSubscription = CompositeDisposable()

    override fun attachView(view: UploadView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe { invalidate() }
        subscriptions += view.categorySelectedClicks().subscribe { categoryItem ->
            onCategorySelected(categoryItem)
        }
        subscriptions += view.categoryClearedClicks().subscribe {
            onCategorySelected()
        }
        subscriptions += view.versionClicks().subscribe { versionItem ->
            router?.openDetailsScreen(appId = versionItem.appId, label = versionItem.title)
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
        putParcelable(KEY_PACKAGE_INFO, packageInfo)
        putParcelable(KEY_CHECK_EXIST, checkExist)
        putParcelable(KEY_CATEGORY_ID, category)
        putString(KEY_WHATS_NEW, whatsNew)
        putString(KEY_DESCRIPTION, description)
        putBoolean(KEY_EXCLUSIVE, exclusive)
        putBoolean(KEY_OPEN_SOURCE, openSource)
        putString(KEY_SOURCE_URL, sourceUrl)
    }

    override fun onAppSelected(info: CommonItem) {
        onPackageChanged(info)
    }

    override fun onDiscardClick() {
        onPackageChanged(info = null)
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun checkAppUploaded() {
        val packageInfo = packageInfo ?: return
        subscriptions += interactor
            .calculateSha1(packageInfo.path)
            .flatMap { interactor.checkExist(it, packageInfo.packageName) }
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    if (it is HttpException) {
                        throw it
                    }
                    println("[upload] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
            .doOnSubscribe {
                view?.hideError()
                view?.showProgress()
            }
            .subscribe(
                { onCheckExistLoaded(it) },
                { onCheckExistError() }
            )
    }

    private fun onCheckExistLoaded(response: CheckExistResponse) {
        this.checkExist = response
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
            packageInfo,
            checkExist,
            category,
            whatsNew,
            description,
            exclusive,
            openSource,
            sourceUrl
        )

        bindItems()

        view?.contentUpdated()
    }

    private fun bindItems() {
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
    }

    private fun onPackageChanged(info: CommonItem?) {
        this.packageInfo = info
        this.checkExist = null
        invalidate()
    }

    private fun invalidate() {
        if (checkExist == null && packageInfo != null) {
            checkAppUploaded()
        } else {
            bindForm()
        }
        subscribeStatusChange(packageInfo)
    }

    private fun subscribeStatusChange(info: CommonItem?) {
        statusSubscription.clear()
        info ?: return
        statusSubscription += uploadManager.status(id = info.path)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .subscribe({ state ->
                when (state.status) {
                    UploadStatus.IDLE,
                    UploadStatus.COMPLETED -> view?.showContent()

                    UploadStatus.AWAIT -> {
                        view?.resetUploadProgress()
                        view?.showUploadProgress()
                    }

                    UploadStatus.ERROR -> view?.showError()
                    UploadStatus.STARTED -> view?.showUploadProgress()
                    else -> view?.setUploadProgress(state.percent / 100f)
                }
            }, {})
    }

    override fun onSelectAppClick() {
        router?.openSelectAppScreen()
    }

    override fun onNoticeClick() {
        val file = checkExist?.file ?: return
        router?.openDetailsScreen(appId = file.appId, label = file.title)
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
        val packageInfo = packageInfo ?: return
//        val category = category ?: return
        val meta = MetaInfo(
            categoryId = 1,//category.id,
            description,
            whatsNew,
            exclusive,
            openSource,
            sourceUrl
        )

        router?.startUpload(packageInfo, meta)
    }

    override fun onOtherVersionsClick(items: List<VersionItem>) {
        view?.showVersionsDialog(items)
    }

    private fun loadCategories() {
        subscriptions += categoriesInteractor.getCategories()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    println("[upload categories] Retry after exception: " + it.message)
                    Observable.timer(3, TimeUnit.SECONDS)
                }
            }
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
private const val KEY_CHECK_EXIST = "check_exist"
private const val KEY_CATEGORY_ID = "category"
private const val KEY_WHATS_NEW = "whats_new"
private const val KEY_DESCRIPTION = "description"
private const val KEY_EXCLUSIVE = "exclusive"
private const val KEY_OPEN_SOURCE = "open_source"
private const val KEY_SOURCE_URL = "source_url"
