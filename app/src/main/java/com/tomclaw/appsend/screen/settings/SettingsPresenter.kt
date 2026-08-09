package com.tomclaw.appsend.screen.settings

import android.os.Bundle
import com.tomclaw.appsend.core.content.ContentFlag
import com.tomclaw.appsend.download.ApkStorage
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.getParcelableArrayListCompat
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

    /**
     * The vocabulary and the last selection the server confirmed. Kept
     * so a rejected change can be put back on screen without another
     * round trip — the switches move first and are corrected only if the
     * write does not land.
     */
    private var catalog: List<ContentFlag> =
        state?.getParcelableArrayListCompat(KEY_CONTENT_CATALOG, ContentFlag::class.java)
            .orEmpty()
    private var contentFilter: Set<String>? =
        state?.getStringArrayList(KEY_CONTENT_FILTER)?.toSet()

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

        // The switches report what moved; what that means for the whole
        // set is decided here, where the last confirmed one is held.
        subscriptions += view.contentFlagChanges().subscribe { change ->
            val current = contentFilter.orEmpty()
            saveContentFilter(
                if (change.shown) current - change.code else current + change.code
            )
        }

        subscriptions += view.familyFriendlyChanges().subscribe { on ->
            saveContentFilter(if (on) catalog.mapTo(mutableSetOf()) { it.code } else emptySet())
        }

        // Paint what we already know, then ask anyway: the value can
        // have moved on the screen this one opens, or on the website,
        // and a settings row showing yesterday's answer is worse than a
        // moment of nothing.
        contentFilter?.let { view.showContentFilter(catalog, it) }
        loadContentFilter()
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

    override fun saveState() = Bundle().apply {
        putParcelableArrayList(KEY_CONTENT_CATALOG, ArrayList(catalog))
        contentFilter?.let { putStringArrayList(KEY_CONTENT_FILTER, ArrayList(it)) }
    }

    private fun loadContentFilter() {
        if (contentFilter == null) {
            view?.setContentFilterEnabled(false)
        }
        subscriptions += settingsInteractor.loadContentFilter()
            .observeOn(schedulers.mainThread())
            .subscribe(
                { state ->
                    // An empty catalog is a server fault, and the block
                    // must not be touchable on one: with nothing to offer,
                    // "hide everything" would send an empty list and
                    // quietly clear whatever the account had set.
                    if (state.catalog.isEmpty()) {
                        view?.showContentFilterUnavailable()
                        return@subscribe
                    }
                    catalog = state.catalog
                    contentFilter = state.hidden
                    view?.showContentFilter(state.catalog, state.hidden)
                    view?.setContentFilterEnabled(true)
                },
                {
                    // No account, no filter to edit: the value belongs to
                    // one. Anything else that goes wrong lands here too,
                    // and the honest answer is the same — not now.
                    view?.showContentFilterUnavailable()
                }
            )
    }

    private fun saveContentFilter(codes: Set<String>) {
        if (codes == contentFilter) return
        val previous = contentFilter
        contentFilter = codes
        view?.showContentFilter(catalog, codes)
        subscriptions += settingsInteractor.updateContentFilter(codes)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { saved ->
                    contentFilter = saved
                    view?.showContentFilter(catalog, saved)
                    analytics.trackEvent("settings-content-filter-changed")
                },
                {
                    contentFilter = previous
                    previous?.let { view?.showContentFilter(catalog, it) }
                    view?.showContentFilterError()
                }
            )
    }

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

private const val KEY_CONTENT_CATALOG = "content_catalog"
private const val KEY_CONTENT_FILTER = "content_filter"
