package com.tomclaw.appsend.screen.home

import android.os.Bundle
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.home.api.ModerationData
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

        fun openAppScreen(appId: String, title: String)

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
    private var startupLoaded: Boolean = state?.getBoolean(KEY_STARTUP_LOADED) ?: false
    private var unread: Int = state?.getInt(KEY_UNREAD) ?: 0
    private var update: AppEntity? = state?.getParcelableCompat(KEY_UPDATE, AppEntity::class.java)
    private var moderation: ModerationData? = state?.getParcelableCompat(KEY_MODERATION, ModerationData::class.java)

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: HomeView) {
        this.view = view

        subscriptions += view.storeClicks().subscribe { bindTab(index = INDEX_STORE) }
        subscriptions += view.discussClicks().subscribe { bindTab(index = INDEX_DISCUSS) }
        subscriptions += view.profileClicks().subscribe { bindTab(index = INDEX_PROFILE) }
        subscriptions += view.uploadClicks().subscribe { router?.openUploadScreen() }
        subscriptions += view.updateClicks().subscribe { onUpdateClicks() }
        subscriptions += view.laterClicks().subscribe { onLaterClicks() }
        subscriptions += view.searchClicks().subscribe { router?.openSearchScreen() }
        subscriptions += view.moderationClicks().subscribe { router?.openModerationScreen() }
        subscriptions += view.installedClicks().subscribe { router?.openInstalledScreen() }
        subscriptions += view.distroClicks().subscribe { router?.openDistroScreen() }
        subscriptions += view.settingsClicks().subscribe { router?.openSettingsScreen() }
        subscriptions += view.aboutClicks().subscribe { router?.openAboutScreen() }

        if (startupLoaded) {
            bindUnread()
            bindUpdate()
            bindTab()
        } else {
            loadStartup()
        }
    }

    private fun bindTab(index: Int = tabIndex) {
        tabIndex = index
        when (index) {
            INDEX_STORE -> {
                router?.showStoreFragment()
                view?.showStoreToolbar(canModerate())
                view?.showUploadButton()
            }

            INDEX_DISCUSS -> {
                router?.showTopicsFragment()
                view?.showDiscussToolbar()
                view?.hideUploadButton()
                bindUnread(count = 0)
            }

            INDEX_PROFILE -> {
                router?.showProfileFragment()
                view?.showProfileToolbar()
                view?.hideUploadButton()
            }
        }
    }

    private fun canModerate(): Boolean = moderation?.moderator ?: false

    private fun loadStartup() {
        subscriptions += interactor.loadStartup()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onStartupLoaded(response = it) },
                { }
            )
    }

    private fun onStartupLoaded(response: StartupResponse) {
        startupLoaded = true
        unread = response.unread
        update = response.update
        moderation = response.moderation

        bindUnread()
        bindUpdate()
        bindTab()
    }

    private fun bindUnread(count: Int = unread) {
        unread = count
        if (unread > 0) {
            view?.showUnreadBadge(count)
        } else {
            view?.hideUnreadBadge()
        }
    }

    private fun bindUpdate() {
        if (update != null) {
            view?.showUpdateBlock()
        } else {
            view?.hideUpdateBlock()
        }
    }

    private fun onUpdateClicks() {
        val update = update ?: return
        router?.openAppScreen(appId = update.appId, title = update.title)
    }

    private fun onLaterClicks() {
        update = null
        view?.hideUpdateBlock()
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
        putBoolean(KEY_STARTUP_LOADED, startupLoaded)
        putInt(KEY_UNREAD, unread)
        putParcelable(KEY_UPDATE, update)
        putParcelable(KEY_MODERATION, moderation)
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
private const val KEY_STARTUP_LOADED = "startup_loaded"
private const val KEY_UNREAD = "unread"
private const val KEY_UPDATE = "update"
private const val KEY_MODERATION = "moderation"
private const val INDEX_STORE = 0
private const val INDEX_DISCUSS = 1
private const val INDEX_PROFILE = 2
