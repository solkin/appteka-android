package com.tomclaw.appsend.screen.upload

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.core.PackageInfoProvider
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.upload.adapter.ItemListener
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem
import com.tomclaw.appsend.screen.upload.adapter.screen_image.ScreenImageItem
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.screen.upload.dto.UploadScreenshot
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadInfo
import com.tomclaw.appsend.upload.UploadManager
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.upload.UploadStatus
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.createApkIconURI
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import com.tomclaw.bananalytics.Bananalytics
import com.tomclaw.bananalytics.api.BreadcrumbCategory
import dagger.Lazy
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface UploadPresenter : ItemListener {

    fun attachView(view: UploadView)

    fun detachView()

    fun attachRouter(router: UploadRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onAppSelected(pkg: UploadPackage, apk: UploadApk)

    fun onFileSelected(uri: Uri)

    fun onAuthorized()

    fun onImagesSelected(images: List<UploadScreenshot>)

    fun onAgreementAccepted()

    fun onAgreementDeclined()

    fun onBackPressed()

    interface UploadRouter {

        fun openApkPicker()

        fun openInstalledPicker()

        fun openDetailsScreen(appId: String, label: String?, isFinish: Boolean)

        fun startUpload(pkg: UploadPackage, apk: UploadApk?, info: UploadInfo)

        fun openLoginScreen()

        fun openImagePicker()

        fun openGallery(items: List<GalleryItem>, current: Int)

        fun openAgreementScreen()

        fun leaveScreen()

        fun hideKeyboard()

    }

}

class UploadPresenterImpl(
    startPackage: UploadPackage?,
    startApkInfo: UploadApk?,
    startInfo: UploadInfo?,
    private val bananalytics: Bananalytics,
    private val interactor: UploadInteractor,
    private val categoriesInteractor: CategoriesInteractor,
    private val categoryConverter: CategoryConverter,
    private val uploadConverter: UploadConverter,
    private val descriptionValidator: DescriptionValidator,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val uploadManager: UploadManager,
    private val packageInfoProvider: PackageInfoProvider,
    private val preferences: UploadPreferencesProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UploadPresenter {

    private var view: UploadView? = null
    private var router: UploadPresenter.UploadRouter? = null

    private var pkg: UploadPackage? = state
        ?.getParcelableCompat(KEY_PACKAGE_INFO, UploadPackage::class.java)
        ?: startPackage
    private var apk: UploadApk? = state
        ?.getParcelableCompat(KEY_APK_INFO, UploadApk::class.java)
        ?: startApkInfo
    private var checkExist: CheckExistResponse? = state
        ?.getParcelableCompat(KEY_CHECK_EXIST, CheckExistResponse::class.java)
        ?: startInfo?.checkExist
    private var screenshots: ArrayList<UploadScreenshot> = state
        ?.getParcelableArrayListCompat(KEY_SCREENSHOTS, UploadScreenshot::class.java)
        ?: ArrayList(startInfo?.screenshots ?: emptyList())
    private var category: CategoryItem? =
        state?.getParcelableCompat(KEY_CATEGORY_ID, CategoryItem::class.java) ?: startInfo?.category
    private var whatsNew: String = state?.getString(KEY_WHATS_NEW) ?: startInfo?.whatsNew.orEmpty()
    private var description: String =
        state?.getString(KEY_DESCRIPTION) ?: startInfo?.description.orEmpty()
    private var exclusive: Boolean =
        (state?.getBoolean(KEY_EXCLUSIVE) ?: startInfo?.exclusive) == true
    private var openSource: Boolean =
        (state?.getBoolean(KEY_OPEN_SOURCE) ?: startInfo?.openSource) == true
    private var sourceUrl: String =
        state?.getString(KEY_SOURCE_URL) ?: startInfo?.sourceUrl.orEmpty()
    private var highlightErrors: Boolean = state?.getBoolean(KEY_HIGHLIGHT_ERRORS) == true
    private var selectedPrefillVersion: VersionItem? = null
    private var isPrefillLoading: Boolean = false

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
        subscriptions += view.pickAppClicks().subscribe { menu ->
            when (menu) {
                MENU_APK -> router?.openApkPicker()
                MENU_INSTALLED -> router?.openInstalledPicker()
            }
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
        putParcelableArrayList(KEY_SCREENSHOTS, screenshots)
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

    override fun onFileSelected(uri: Uri) {
        subscriptions += interactor
            .uriToFile(uri)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { file ->
                    packageInfoProvider.getPackageInfo(file.absolutePath)?.let { packageInfo ->
                        onAppSelected(
                            pkg = UploadPackage(
                                uniqueId = file.path,
                                sha1 = null,
                                packageName = packageInfo.packageName,
                                size = file.length()
                            ),
                            apk = UploadApk(
                                path = file.path,
                                version = packageInfo.versionName.orEmpty(),
                                size = file.length(),
                                packageInfo = packageInfo
                            ),
                        )
                    }
                },
                { }
            )
    }

    override fun onAuthorized() {
        invalidate()
    }

    override fun onImagesSelected(images: List<UploadScreenshot>) {
        screenshots = ArrayList((screenshots + images).distinctBy { it.original })
        bindForm()
    }

    override fun onAgreementAccepted() {
        preferences.setAgreementAccepted(value = true)
        startUpload()
    }

    override fun onAgreementDeclined() {
        preferences.setAgreementAccepted(value = false)
        view?.showAgreementError()
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
            subscribeStatusChange(pkg)
            return
        }

        subscriptions += sha1Observer
            .flatMap { interactor.checkExist(it, pkg.packageName, pkg.size) }
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideError()
                view?.showProgress()
            }
            .doAfterTerminate {
                subscribeStatusChange(pkg)
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
            this.screenshots = meta.screenshots?.map {
                UploadScreenshot(
                    it.scrId,
                    it.original.toUri(),
                    it.preview.toUri(),
                    it.width,
                    it.height
                )
            }?.let { ArrayList(it) } ?: ArrayList()
            this.category = meta.category?.let { categoryConverter.convert(it) } ?: this.category
            this.whatsNew = meta.whatsNew.orEmpty()
            this.description = meta.description.orEmpty()
            this.exclusive = meta.exclusive == true
            this.openSource = meta.openSource ?: !meta.sourceUrl.isNullOrEmpty()
            this.sourceUrl = meta.sourceUrl.orEmpty()
        }
        view?.showContent()
        bindForm()
        bindUploadAppIcon()
    }

    private fun onCheckExistError() {
        view?.showContent()
        view?.showError()
    }

    private fun updateItems() {
        items.clear()
        items += uploadConverter.convert(
            pkg,
            apk,
            checkExist,
            screenshots,
            category,
            whatsNew,
            description,
            exclusive,
            openSource,
            sourceUrl,
            highlightErrors,
            selectedPrefillVersion,
        )
    }

    private fun bindForm() {
        updateItems()
        bindItems()
        view?.contentUpdated()
    }

    private fun bindItems() {
        adapterPresenter.get().onDataSourceChanged(items)
    }

    private fun bindUploadAppIcon() {
        when {
            apk?.packageInfo != null -> createApkIconURI(apk?.path.orEmpty())
            checkExist?.file?.icon != null -> checkExist?.file?.icon
            else -> null
        }?.let { view?.setAppIcon(it) }
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
            subscribeStatusChange(pkg)
        }
    }

    private fun clearForm() {
        screenshots = ArrayList()
        category = null
        whatsNew = ""
        description = ""
        exclusive = false
        openSource = false
        sourceUrl = ""
        highlightErrors = false
        selectedPrefillVersion = null
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
        view?.showUploadDialog()
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
        updateItems()
    }

    override fun onDescriptionChanged(text: String) {
        description = text
        updateItems()
    }

    override fun onExclusiveChanged(value: Boolean) {
        exclusive = value
        updateItems()
    }

    override fun onOpenSourceChanged(value: Boolean, url: String) {
        openSource = value
        sourceUrl = url
        updateItems()
    }

    override fun onSubmitClick() {
        bananalytics.leaveBreadcrumb("Submit upload clicked", BreadcrumbCategory.USER_ACTION)
        highlightErrors = true
        val uploadStarted = startUpload()
        if (!uploadStarted) {
            bindForm()
            view?.scrollToTop()
        }
    }

    private fun startUpload(): Boolean {
        if (!preferences.isAgreementAccepted()) {
            router?.openAgreementScreen()
            return false
        }

        val pkg = pkg ?: return false
        val category = category ?: return false
        val checkExist = checkExist ?: return false
        description.ifBlank { null } ?: return false
        if (!descriptionValidator.isValid(description)) return false

        val info = UploadInfo(
            checkExist,
            screenshots,
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

    override fun onScreenAppendClick() {
        router?.openImagePicker()
    }

    override fun onScreenshotClick(item: ScreenImageItem) {
        router?.openGallery(
            items = screenshots.map { GalleryItem(it.original, it.width, it.height) },
            current = screenshots.indexOfFirst { it.original == item.original },
        )
    }

    override fun onScreenshotDelete(item: ScreenImageItem) {
        screenshots.remove(screenshots.first { it.original == item.original })
        bindForm()
    }

    override fun onPrefillVersionSelected(version: VersionItem?) {
        if (version == null) {
            // Clear prefill - reset to empty form (but keep whatsNew as is)
            selectedPrefillVersion = null
            screenshots = ArrayList()
            category = null
            description = ""
            exclusive = false
            openSource = false
            sourceUrl = ""
            bindForm()
            return
        }

        if (isPrefillLoading) return
        isPrefillLoading = true
        selectedPrefillVersion = version

        subscriptions += interactor.loadVersionMeta(version.appId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe { view?.showProgress() }
            .doAfterTerminate {
                isPrefillLoading = false
                view?.showContent()
            }
            .subscribe(
                { details -> onPrefillMetaLoaded(details) },
                { onPrefillMetaError() }
            )
    }

    private fun onPrefillMetaLoaded(details: com.tomclaw.appsend.screen.details.api.Details) {
        details.meta?.let { meta ->
            this.screenshots = meta.screenshots?.map {
                UploadScreenshot(
                    it.scrId,
                    it.original.toUri(),
                    it.preview.toUri(),
                    it.width,
                    it.height
                )
            }?.let { ArrayList(it) } ?: ArrayList()
            this.category = meta.category?.let { categoryConverter.convert(it) }
            // Note: whatsNew is NOT prefilled as per requirements
            this.description = meta.description.orEmpty()
            this.exclusive = meta.exclusive == true
            this.openSource = meta.openSource ?: !meta.sourceUrl.isNullOrEmpty()
            this.sourceUrl = meta.sourceUrl.orEmpty()
        }
        bindForm()
    }

    private fun onPrefillMetaError() {
        selectedPrefillVersion = null
        view?.showError()
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
private const val KEY_SCREENSHOTS = "screenshots"
private const val KEY_CATEGORY_ID = "category"
private const val KEY_WHATS_NEW = "whats_new"
private const val KEY_DESCRIPTION = "description"
private const val KEY_EXCLUSIVE = "exclusive"
private const val KEY_OPEN_SOURCE = "open_source"
private const val KEY_SOURCE_URL = "source_url"
private const val KEY_HIGHLIGHT_ERRORS = "highlight_errors"
