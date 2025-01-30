package com.tomclaw.appsend.screen.home

import android.os.Bundle
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.home.api.ModerationData
import com.tomclaw.appsend.screen.home.api.StartupResponse
import com.tomclaw.appsend.screen.home.api.StatusResponse
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

        fun showFeedFragment()

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

        fun openShareUrlDialog(text: String)

        fun leaveScreen()

        fun exitApp()

    }

}

class HomePresenterImpl(
    startAction: String?,
    private val interactor: HomeInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : HomePresenter {

    private var view: HomeView? = null
    private var router: HomePresenter.HomeRouter? = null

    private var action: String? = state?.getString(KEY_ACTION) ?: startAction
    private var tabIndex: Int = state?.getInt(KEY_TAB_INDEX) ?: INDEX_STORE
    private var startupLoaded: Boolean = state?.getBoolean(KEY_STARTUP_LOADED) ?: false
    private var unread: Int = state?.getInt(KEY_UNREAD) ?: 0
    private var feed: Int = state?.getInt(KEY_UNREAD) ?: 0
    private var update: AppEntity? = state?.getParcelableCompat(KEY_UPDATE, AppEntity::class.java)
    private var moderation: ModerationData? =
        state?.getParcelableCompat(KEY_MODERATION, ModerationData::class.java)
    private var status: StatusResponse? =
        state?.getParcelableCompat(KEY_STATUS, StatusResponse::class.java)

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: HomeView) {
        this.view = view

        subscriptions += view.storeClicks().subscribe { bindTab(index = INDEX_STORE) }
        subscriptions += view.feedClicks().subscribe { bindTab(index = INDEX_FEED) }
        subscriptions += view.discussClicks().subscribe { bindTab(index = INDEX_DISCUSS) }
        subscriptions += view.profileClicks().subscribe { bindTab(index = INDEX_PROFILE) }
        subscriptions += view.uploadClicks().subscribe { router?.openUploadScreen() }
        subscriptions += view.updateClicks().subscribe { onUpdateClicks() }
        subscriptions += view.laterClicks().subscribe { onLaterClicks() }
        subscriptions += view.searchClicks().subscribe { router?.openSearchScreen() }
        subscriptions += view.moderationClicks().subscribe { router?.openModerationScreen() }
        subscriptions += view.profileShareClicks().subscribe { onShareClicks() }
        subscriptions += view.installedClicks().subscribe { router?.openInstalledScreen() }
        subscriptions += view.distroClicks().subscribe { router?.openDistroScreen() }
        subscriptions += view.settingsClicks().subscribe { router?.openSettingsScreen() }
        subscriptions += view.aboutClicks().subscribe { router?.openAboutScreen() }
        subscriptions += view.exitAppClicks().subscribe { router?.exitApp() }

        if (startupLoaded) {
            bindUnread()
            bindFeed()
            bindUpdate()
            bindTab()
        } else {
            loadStartup()
        }

        val status = status
        if (status != null) {
            onStatusLoaded(status)
        } else {
            loadStatus()
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

            INDEX_FEED -> {
                router?.showFeedFragment()
                view?.showFeedToolbar()
                view?.hideUploadButton()
                bindFeed(count = 0)
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

    private fun loadStatus() {
        subscriptions += interactor.loadStatus()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { onStatusLoaded(response = it) },
                { }
            )
    }

    private fun onStartupLoaded(response: StartupResponse) {
        startupLoaded = true
        unread = response.unread
        feed = response.feed
        update = response.update
        moderation = response.moderation

        bindUnread()
        bindFeed()
        bindUpdate()
        bindTab()
    }

    private fun onStatusLoaded(response: StatusResponse) {
        status = response
        if (!response.message.isNullOrBlank()) {
            view?.showStatusDialog(
                block = response.block ?: false,
                title = response.title,
                message = response.message,
            )
        }
    }

    private fun bindUnread(count: Int = unread) {
        unread = count
        if (unread > 0) {
            view?.showUnreadBadge(count)
        } else {
            view?.hideUnreadBadge()
        }
    }

    private fun bindFeed(count: Int = feed) {
        feed = count
        if (feed > 0) {
            view?.showFeedBadge(count)
        } else {
            view?.hideFeedBadge()
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

    private fun onShareClicks() {
        subscriptions += interactor.getUserBrief()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { it.url?.let { url -> router?.openShareUrlDialog(url) } },
                { }
            )
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: HomePresenter.HomeRouter) {
        this.router = router
        handleAction()
        bindTab()
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putString(KEY_ACTION, action)
        putInt(KEY_TAB_INDEX, tabIndex)
        putBoolean(KEY_STARTUP_LOADED, startupLoaded)
        putInt(KEY_UNREAD, unread)
        putInt(KEY_FEED, feed)
        putParcelable(KEY_UPDATE, update)
        putParcelable(KEY_MODERATION, moderation)
        putParcelable(KEY_STATUS, status)
    }

    override fun onBackPressed() {
        if (tabIndex != INDEX_STORE) {
            view?.selectStoreTab()
        } else {
            router?.leaveScreen()
        }
    }

    private fun handleAction() {
        when (action) {
            ACTION_STORE -> view?.selectStoreTab()
            ACTION_DISCUSS -> view?.selectDiscussTab()
            ACTION_APPS -> router?.openInstalledScreen()
            ACTION_INSTALL -> router?.openDistroScreen()
        }
        action = ""
    }

}

private const val KEY_ACTION = "action"
private const val KEY_TAB_INDEX = "current_tab"
private const val KEY_STARTUP_LOADED = "startup_loaded"
private const val KEY_UNREAD = "unread"
private const val KEY_FEED = "feed"
private const val KEY_UPDATE = "update"
private const val KEY_MODERATION = "moderation"
private const val KEY_STATUS = "status"
private const val INDEX_STORE = 0
private const val INDEX_FEED = 1
private const val INDEX_DISCUSS = 2
private const val INDEX_PROFILE = 3

const val ACTION_STORE = "com.tomclaw.appsend.cloud"
const val ACTION_DISCUSS = "com.tomclaw.appsend.discuss"
const val ACTION_APPS = "com.tomclaw.appsend.apps"
const val ACTION_INSTALL = "com.tomclaw.appsend.install"
