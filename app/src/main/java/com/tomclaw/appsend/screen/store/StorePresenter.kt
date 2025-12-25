package com.tomclaw.appsend.screen.store

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
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

    private var dropdownItems: List<CategoryDropdownItem> =
        state?.getParcelableArrayListCompat(KEY_DROPDOWN_ITEMS, CategoryDropdownItem::class.java)
            ?: emptyList()
    private var selectedPosition: Int = state?.getInt(KEY_SELECTED_POSITION) ?: 0

    override fun attachView(view: StoreView) {
        this.view = view

        subscriptions += view.retryClicks().subscribe {
            loadApps()
        }
        subscriptions += view.refreshClicks().subscribe {
            invalidateApps()
            analytics.trackEvent("store-refresh")
        }
        subscriptions += view.categorySelectedClicks().subscribe { position ->
            onCategoryPositionSelected(position)
        }

        if (dropdownItems.isNotEmpty()) {
            // Restore from saved state synchronously
            view.showCategories(dropdownItems)
            view.setSelectedCategory(dropdownItems[selectedPosition])
            if (isError) {
                onError()
            } else {
                items?.let { bindItems() } ?: loadApps()
            }
        } else {
            // Load categories first, then load apps
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
        putInt(KEY_SELECTED_POSITION, selectedPosition)
    }

    override fun invalidateApps() {
        items = null
        isError = false
        loadApps()
    }

    private fun loadApps() {
        subscriptions += storeInteractor.listApps(categoryId = category?.id)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { if (view?.isPullRefreshing() == false) view?.showProgress() }
            .subscribe(
                { onLoaded(it) },
                { onError() }
            )
    }

    private fun loadApps(offsetAppId: String) {
        subscriptions += storeInteractor.listApps(offsetAppId, category?.id)
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

        // Find position of saved category
        selectedPosition = if (category != null) {
            dropdownItems.indexOfFirst { it.id == category?.id }.takeIf { it >= 0 } ?: 0
        } else {
            0
        }

        view?.showCategories(dropdownItems)
        view?.setSelectedCategory(dropdownItems[selectedPosition])

        if (isError) {
            onError()
        } else {
            items?.let { bindItems() } ?: loadApps()
        }
    }

    private fun onCategoryPositionSelected(position: Int) {
        if (position == selectedPosition) return

        selectedPosition = position
        val selectedItem = dropdownItems.getOrNull(position)

        if (selectedItem == null || selectedItem.id == 0) {
            category = null
            analytics.trackEvent("store-category-cleared")
        } else {
            category = CategoryItem(
                id = selectedItem.id,
                title = selectedItem.title,
                icon = selectedItem.iconSvg ?: ""
            )
            analytics.trackEvent("store-category-selected")
        }

        view?.setSelectedCategory(dropdownItems[selectedPosition])
        view?.scrollToTop()
        invalidateApps()
    }

}

private const val KEY_APPS = "apps"
private const val KEY_ERROR = "error"
private const val KEY_CATEGORY_ID = "category"
private const val KEY_DROPDOWN_ITEMS = "dropdown_items"
private const val KEY_SELECTED_POSITION = "selected_position"
