package com.tomclaw.appsend.screen.home

import android.os.Bundle
import com.tomclaw.appsend.screen.home.api.StartupResponse
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableCompat
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface HomePresenter {

    fun attachView(view: HomeView)

    fun detachView()

    fun attachRouter(router: HomeRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface HomeRouter {

        fun showStoreFragment()

        fun showTopicsFragment()

        fun showProfileFragment()

        fun openUploadScreen()

        fun openSearchScreen()

        fun openModerationScreen()

        fun openInstalledScreen()

        fun openDistroScreen()

        fun openSettingsScreen()

        fun openAboutScreen()

        fun leaveScreen()

    }

}

class HomePresenterImpl(
    private val interactor: HomeInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : HomePresenter {

    private var view: HomeView? = null
    private var router: HomePresenter.HomeRouter? = null

    private var tabIndex: Int = state?.getInt(KEY_TAB_INDEX) ?: INDEX_STORE
    private var startup: StartupResponse? =
        state?.getParcelableCompat(KEY_STARTUP, StartupResponse::class.java)

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: HomeView) {
        this.view = view

        subscriptions += view.storeClicks().subscribe { bindTab(index = INDEX_STORE) }
        subscriptions += view.discussClicks().subscribe { bindTab(index = INDEX_DISCUSS) }
        subscriptions += view.profileClicks().subscribe { bindTab(index = INDEX_PROFILE) }
        subscriptions += view.uploadClicks().subscribe { router?.openUploadScreen() }
        subscriptions += view.updateClicks().subscribe { }
        subscriptions += view.laterClicks().subscribe {  }
        subscriptions += view.searchClicks().subscribe { router?.openSearchScreen() }
        subscriptions += view.moderationClicks().subscribe { router?.openModerationScreen() }
        subscriptions += view.installedClicks().subscribe { router?.openInstalledScreen() }
        subscriptions += view.distroClicks().subscribe { router?.openDistroScreen() }
        subscriptions += view.settingsClicks().subscribe { router?.openSettingsScreen() }
        subscriptions += view.aboutClicks().subscribe { router?.openAboutScreen() }

        if (startup != null) {
            bindStartup()
        } else {
            loadStartup()
        }
    }

    private fun bindTab(index: Int = tabIndex) {
        tabIndex = index
        when (index) {
            INDEX_STORE -> {
                router?.showStoreFragment()
                view?.showStoreToolbar(canModerate = startup?.moderation?.moderator ?: false)
                view?.showUploadButton()
            }

            INDEX_DISCUSS -> {
                router?.showTopicsFragment()
                view?.showDiscussToolbar()
                view?.hideUploadButton()
                view?.hideUnreadBadge()
            }

            INDEX_PROFILE -> {
                router?.showProfileFragment()
                view?.showProfileToolbar()
                view?.hideUploadButton()
            }
        }
    }

    private fun loadStartup() {
        subscriptions += interactor.loadStartup()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onStartupLoaded(response = it) },
                { }
            )
    }

    private fun onStartupLoaded(response: StartupResponse) {
        startup = response
        bindStartup()
    }

    private fun bindStartup() {
        val startup = startup ?: return
        if (startup.unread > 0) {
            view?.showUnreadBadge(count = startup.unread)
        } else {
            view?.hideUnreadBadge()
        }
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: HomePresenter.HomeRouter) {
        this.router = router
        bindTab()
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putInt(KEY_TAB_INDEX, tabIndex)
        putParcelable(KEY_STARTUP, startup)
    }

    override fun onBackPressed() {
        if (tabIndex != INDEX_STORE) {
            view?.selectStoreTab()
        } else {
            router?.leaveScreen()
        }
    }

}

private const val KEY_TAB_INDEX = "current_tab"
private const val KEY_STARTUP = "startup"
private const val INDEX_STORE = 0
private const val INDEX_DISCUSS = 1
private const val INDEX_PROFILE = 2
