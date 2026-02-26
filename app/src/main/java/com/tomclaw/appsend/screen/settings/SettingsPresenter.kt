package com.tomclaw.appsend.screen.settings

import android.os.Bundle
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface SettingsPresenter {

    fun attachView(view: SettingsView)

    fun detachView()

    fun attachRouter(router: SettingsRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface SettingsRouter {

        fun finishActivity()

        fun restartActivity()

        fun setResultOk()

        fun requestStoragePermissions(callback: (Boolean) -> Unit)

    }

}

class SettingsPresenterImpl(
    private val settingsInteractor: SettingsInteractor,
    private val apkStorage: ApkStorage,
    private val resourceProvider: SettingsResourceProvider,
    private val analytics: Analytics,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : SettingsPresenter {

    private var view: SettingsView? = null
    private var router: SettingsPresenter.SettingsRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: SettingsView) {
        this.view = view

        subscriptions += view.clearCacheClicks().subscribe {
            clearCache()
        }

        subscriptions += settingsInteractor.observePreferenceChanges()
            .observeOn(schedulers.mainThread())
            .subscribe { change ->
                onPreferenceChanged(change)
            }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: SettingsPresenter.SettingsRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle()

    private fun clearCache() {
        if (apkStorage.isPermissionRequired()) {
            router?.requestStoragePermissions { granted ->
                if (granted) {
                    doClearCache()
                }
            }
        } else {
            doClearCache()
        }
    }

    private fun doClearCache() {
        subscriptions += settingsInteractor.clearCache()
            .observeOn(schedulers.mainThread())
            .subscribe(
                {
                    view?.showCacheClearedMessage()
                    analytics.trackEvent("settings-cache-cleared")
                },
                {
                    view?.showCacheClearErrorMessage()
                    analytics.trackEvent("settings-cache-clear-failed")
                }
            )
    }

    private fun onPreferenceChanged(change: PreferenceChange) {
        when (change.key) {
            resourceProvider.getPrefShowSystemKey() -> {
                val isEnabled = change.value as? Boolean ?: return
                if (isEnabled) {
                    showSystemAppsWarning()
                }
                router?.setResultOk()
            }

            resourceProvider.getPrefSortOrderKey() -> {
                router?.setResultOk()
            }
        }
    }

    private fun showSystemAppsWarning() {
        view?.showSystemAppsWarning(
            title = resourceProvider.getSystemAppsWarningTitle(),
            message = resourceProvider.getSystemAppsWarningMessage(),
            buttonText = resourceProvider.getGotItButtonText()
        )
    }

}
