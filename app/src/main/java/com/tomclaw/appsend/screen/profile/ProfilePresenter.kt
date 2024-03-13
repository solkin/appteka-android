package com.tomclaw.appsend.screen.profile

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.blueprint.Item
import com.avito.konveyor.data_source.ListDataSource
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.screen.profile.adapter.app.AppItem
import com.tomclaw.appsend.screen.profile.adapter.rating.RatingItem
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterUnauthorizedErrors
import com.tomclaw.appsend.util.getParcelableCompat
import com.tomclaw.appsend.util.retryWhenNonAuthErrors
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface ProfilePresenter : ItemListener {

    fun attachView(view: ProfileView)

    fun detachView()

    fun attachRouter(router: ProfileRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface ProfileRouter {

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
    }

    override fun onAppClick(item: AppItem) {

    }

    override fun onRatingClick(item: RatingItem) {

    }

    override fun onFavoritesClick() {

    }

    private fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun loadProfile() {
        subscriptions += interactor.loadProfile(userId)
            .observeOn(schedulers.mainThread())
            .retryWhenNonAuthErrors()
            .doOnSubscribe {
                view?.hideMenu()
                view?.hideError()
                view?.showProgress()
            }
            .subscribe(
                { onProfileLoaded(it) },
                { onLoadingError(it) }
            )
    }

    private fun onProfileLoaded(profile: ProfileResponse) {
        this.profile = profile
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
        items += converter.convert(profile.profile, profile.grantRoles)

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
