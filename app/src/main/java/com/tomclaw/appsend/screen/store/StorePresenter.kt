package com.tomclaw.appsend.screen.store

import android.os.Bundle
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.categories.CategoriesInteractor
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.categories.CategoryConverter
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.store.adapter.ItemListener
import com.tomclaw.appsend.screen.store.adapter.app.AppItem
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface StorePresenter : ItemListener {

    fun attachView(view: StoreView)

    fun detachView()

    fun attachRouter(router: StoreRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onUpdate()

    fun invalidateApps()

    fun scrollToTop()

    interface StoreRouter {

        fun openAppScreen(appId: String, title: String)

    }

}

class StorePresenterImpl(
    private val storeInteractor: StoreInteractor,
    private val categoriesInteractor: CategoriesInteractor,
    private val categoryConverter: CategoryConverter,
    private val dropdownItemConverter: CategoryDropdownItemConverter,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val appConverter: AppConverter,
    private val analytics: Analytics,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : StorePresenter {

    private var view: StoreView? = null
    private var router: StorePresenter.StoreRouter? = null

    private val subscriptions = CompositeDisposable()

    private var items: List<AppItem>? =
        state?.getParcelableArrayListCompat(KEY_APPS, AppItem::class.java)
    private var isError: Boolean = state?.getBoolean(KEY_ERROR) == true
    private var category: CategoryItem? =
        state?.getParcelableCompat(KEY_CATEGORY_ID, CategoryItem::class.java)
    private var openSource: Boolean = state?.getBoolean(KEY_OPEN_SOURCE) == true
    private var exclusive: Boolean = state?.getBoolean(KEY_EXCLUSIVE) == true

    private var dropdownItems: List<CategoryDropdownItem> =
        state?.getParcelableArrayListCompat(KEY_DROPDOWN_ITEMS, CategoryDropdownItem::class.java)
            ?: emptyList()

    override fun attachView(view: StoreView) {
        this.view = view

        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
            analytics.trackEvent("store-refresh")
        }
        subscriptions += view.categorySelectedClicks().subscribe { categoryId ->
            onCategorySelected(categoryId)
        }
        subscriptions += view.openSourceClicks().subscribe { checked ->
            onOpenSourceChanged(checked)
        }
        subscriptions += view.exclusiveClicks().subscribe { checked ->
            onExclusiveChanged(checked)
        }

        view.setFilters(openSource = openSource, exclusive = exclusive)

        if (dropdownItems.isNotEmpty()) {
            // Restore from saved state synchronously
            view.showCategories(dropdownItems)
            selectedCategoryItem()?.let { view.setSelectedCategory(it) }
            if (isError) {
                onError()
            } else {
                items?.let { bindItems() } ?: loadApps()
            }
        } else {
            view.showProgress()
            loadCategories()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: StorePresenter.StoreRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_APPS, items?.let { ArrayList(items.orEmpty()) })
        putBoolean(KEY_ERROR, isError)
        putParcelable(KEY_CATEGORY_ID, category)
        putParcelableArrayList(KEY_DROPDOWN_ITEMS, ArrayList(dropdownItems))
        putBoolean(KEY_OPEN_SOURCE, openSource)
        putBoolean(KEY_EXCLUSIVE, exclusive)
    }

    override fun invalidateApps() {
        items = null
        isError = false
        loadApps()
    }

    private fun loadApps() {
        subscriptions += storeInteractor
            .listApps(categoryId = category?.id, openSource = openSource, exclusive = exclusive)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadApps(offsetAppId: String) {
        subscriptions += storeInteractor
            .listApps(offsetAppId, category?.id, openSource, exclusive)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun onLoaded(entities: List<AppEntity>) {
        isError = false
        val newItems = entities
            .map { appConverter.convert(it) }
            .toList()
            .apply { if (isNotEmpty()) last().hasMore = true }
        this.items = this.items
            ?.apply { if (isNotEmpty()) last().hasProgress = false }
            ?.plus(newItems) ?: newItems
        bindItems()
    }

    private fun bindItems() {
        val items = this.items
        when {
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

    private fun onError() {
        this.isError = true
        view?.showError()
    }

    override fun onUpdate() {
        view?.contentUpdated()
    }

    override fun scrollToTop() {
        view?.scrollToTop()
    }

    override fun onItemClick(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        router?.openAppScreen(app.appId, app.title)
    }

    override fun onLoadMore(item: Item) {
        val app = items?.find { it.id == item.id } ?: return
        loadApps(app.appId)
    }

    private fun loadCategories() {
        subscriptions += categoriesInteractor.getCategories()
            .toObservable()
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { onCategoriesLoaded(it) },
                { onError() }
            )
    }

    private fun onCategoriesLoaded(categories: List<Category>) {
        val categoryItems = categories.map { categoryConverter.convert(it) }.sortedBy { it.title }
        dropdownItems = dropdownItemConverter.convert(categoryItems)

        view?.showCategories(dropdownItems)
        selectedCategoryItem()?.let { view?.setSelectedCategory(it) }

        if (isError) {
            onError()
        } else {
            items?.let { bindItems() } ?: loadApps()
        }
    }

    // The "All categories" entry has id 0, matching a null category.
    private fun selectedCategoryItem(): CategoryDropdownItem? =
        dropdownItems.find { it.id == (category?.id ?: 0) } ?: dropdownItems.firstOrNull()

    private fun onCategorySelected(categoryId: Int) {
        if (categoryId == (category?.id ?: 0)) return
        val item = dropdownItems.find { it.id == categoryId } ?: return

        category = if (categoryId == 0) {
            analytics.trackEvent("store-category-cleared")
            null
        } else {
            analytics.trackEvent("store-category-selected")
            CategoryItem(id = item.id, title = item.title, icon = item.iconSvg ?: "")
        }

        view?.setSelectedCategory(item)
        view?.scrollToTop()
        invalidateApps()
    }

    private fun onOpenSourceChanged(checked: Boolean) {
        if (checked == openSource) return
        openSource = checked
        analytics.trackEvent("store-filter-open-source")
        view?.scrollToTop()
        invalidateApps()
    }

    private fun onExclusiveChanged(checked: Boolean) {
        if (checked == exclusive) return
        exclusive = checked
        analytics.trackEvent("store-filter-exclusive")
        view?.scrollToTop()
        invalidateApps()
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val KEY_CATEGORY_ID = "category"
private const val KEY_DROPDOWN_ITEMS = "dropdown_items"
private const val KEY_OPEN_SOURCE = "open_source"
private const val KEY_EXCLUSIVE = "exclusive"
