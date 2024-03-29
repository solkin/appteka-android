package com.tomclaw.appsend.screen.profile

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItem
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.Observables
import io.reactivex.rxjava3.kotlin.plusAssign

interface ProfilePresenter : ItemListener {

    fun attachView(view: ProfileView)

    fun detachView()

    fun attachRouter(router: ProfileRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface ProfileRouter {

        fun openUserFilesScreen(userId: Int)

        fun openDetailsScreen(appId: String, label: String?)

        fun openFavoriteScreen(userId: Int)

        fun openReviewsScreen(userId: Int)

        fun openLoginScreen()

        fun leaveScreen()

    }

}

class ProfilePresenterImpl(
    private val userId: Int,
    private val interactor: ProfileInteractor,
    private val converter: ProfileConverter,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ProfilePresenter {

    private var view: ProfileView? = null
    private var router: ProfilePresenter.ProfileRouter? = null

    private var profile: ProfileResponse? =
        state?.getParcelableCompat(KEY_PROFILE, ProfileResponse::class.java)
    private var uploads: ArrayList<AppEntity>? =
        state?.getParcelableArrayListCompat(KEY_UPLOADS, AppEntity::class.java)

    private val items = ArrayList<Item>()

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: ProfileView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.swipeRefresh().subscribe { loadProfile() }
        subscriptions += view.shareClicks().subscribe {}
        subscriptions += view.loginClicks().subscribe {
            router?.openLoginScreen()
        }

        if (profile != null) {
            bindProfile()
        } else {
            loadProfile()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ProfilePresenter.ProfileRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
        putParcelable(KEY_PROFILE, profile)
        putParcelableArrayList(KEY_UPLOADS, uploads)
    }

    override fun onAppClick(item: AppItem) {
        router?.openDetailsScreen(item.appId, item.title)
    }

    override fun onRatingClick(item: ReviewItem) {
        val profile = profile?.profile ?: return
        router?.openReviewsScreen(profile.userId)
    }

    override fun onFavoritesClick() {
        val profile = profile?.profile ?: return
        router?.openFavoriteScreen(profile.userId)
    }

    override fun onUploadsClick(userId: Int) {
        router?.openUserFilesScreen(userId)
    }

    override fun onNextPage(last: AppItem, param: (List<AppItem>) -> Unit) {
        subscriptions += interactor.loadUserApps(userId, last.appId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .subscribe(
                { response ->
                    val appItems = converter.convertApps(response.files)
                    param.invoke(appItems)
                },
                { }
            )
    }

    private fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun loadProfile() {
        subscriptions += Observables.zip(
            interactor.loadProfile(userId),
            interactor.loadUserApps(userId, offsetAppId = null)
        )
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.hideError()
                view?.showProgress()
            }
            .subscribe(
                { onProfileLoaded(it.first, it.second.files) },
                { onLoadingError(it) }
            )
    }

    private fun onProfileLoaded(profile: ProfileResponse?, uploads: List<AppEntity>) {
        this.profile = profile
        this.uploads = ArrayList(uploads)
        bindProfile()
    }

    private fun onLoadingError(ex: Throwable) {
        ex.filterUnauthorizedErrors({ view?.showUnauthorizedError() }) {
            view?.hideMenu()
            view?.showContent()
            view?.showError()
        }
    }

    private fun bindProfile() {
        val profile = this.profile ?: return

        items.clear()
        items += converter.convertProfile(profile.profile, profile.grantRoles, uploads)

        bindItems()
        bindMenu()

        view?.contentUpdated()
        view?.showContent()
    }

    private fun bindMenu() {
        view?.showMenu(
            canEliminate = true
        )
    }

    private fun bindItems() {
        val dataSource = ListDataSource(items)
        adapterPresenter.get().onDataSourceChanged(dataSource)
    }

}

private const val KEY_PROFILE = "profile"
private const val KEY_UPLOADS = "uploads"
