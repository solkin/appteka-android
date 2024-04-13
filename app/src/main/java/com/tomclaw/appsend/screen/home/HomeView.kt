package com.tomclaw.appsend.screen.home

import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import io.reactivex.rxjava3.core.Observable

interface HomeView {

    fun showStoreToolbar(canModerate: Boolean)

    fun showDiscussToolbar()

    fun showProfileToolbar()

    fun storeClicks(): Observable<Unit>

    fun discussClicks(): Observable<Unit>

    fun profileClicks(): Observable<Unit>

    fun uploadClicks(): Observable<Unit>

    fun updateClicks(): Observable<Unit>

    fun laterClicks(): Observable<Unit>

    fun searchClicks(): Observable<Unit>

    fun moderationClicks(): Observable<Unit>

    fun installedClicks(): Observable<Unit>

    fun distroClicks(): Observable<Unit>

    fun settingsClicks(): Observable<Unit>

    fun aboutClicks(): Observable<Unit>

}

class HomeViewImpl(view: View) : HomeView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val updateBlock: View = view.findViewById(R.id.update_block)
    private val bottomNavigation: BottomNavigationView = view.findViewById(R.id.bottom_navigation)
    private val uploadButton: FloatingActionButton = view.findViewById(R.id.fab)
    private val updateButton: Button = view.findViewById(R.id.update)
    private val laterButton: Button = view.findViewById(R.id.update_later)

    private val storeRelay = PublishRelay.create<Unit>()
    private val discussRelay = PublishRelay.create<Unit>()
    private val profileRelay = PublishRelay.create<Unit>()
    private val uploadRelay = PublishRelay.create<Unit>()
    private val updateRelay = PublishRelay.create<Unit>()
    private val laterRelay = PublishRelay.create<Unit>()
    private val searchRelay = PublishRelay.create<Unit>()
    private val moderationRelay = PublishRelay.create<Unit>()
    private val installedRelay = PublishRelay.create<Unit>()
    private val distroRelay = PublishRelay.create<Unit>()
    private val settingsRelay = PublishRelay.create<Unit>()
    private val aboutRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_search -> searchRelay.accept(Unit)
                R.id.menu_moderation -> moderationRelay.accept(Unit)
                R.id.menu_installed -> installedRelay.accept(Unit)
                R.id.menu_distro -> distroRelay.accept(Unit)
                R.id.menu_settings -> settingsRelay.accept(Unit)
                R.id.menu_about -> aboutRelay.accept(Unit)
            }
            true
        }

        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.nav_store -> storeRelay.accept(Unit)
                R.id.nav_discuss -> discussRelay.accept(Unit)
                R.id.nav_profile -> profileRelay.accept(Unit)
            }
            true
        }

        uploadButton.clicks(uploadRelay)
        updateButton.clicks(updateRelay)
        laterButton.clicks(laterRelay)
    }

    override fun showStoreToolbar(canModerate: Boolean) {
        with(toolbar) {
            setTitle(R.string.tab_store)
            menu.clear()
            inflateMenu(R.menu.store_menu)
            if (!canModerate) {
                menu.removeItem(R.id.menu_moderation)
            }
            invalidateMenu()
        }
    }

    override fun showDiscussToolbar() {
        with(toolbar) {
            setTitle(R.string.tab_discuss)
            menu.clear()
            inflateMenu(R.menu.home_menu)
            invalidateMenu()
        }
    }

    override fun showProfileToolbar() {
        with(toolbar) {
            setTitle(R.string.tab_profile)
            menu.clear()
            inflateMenu(R.menu.home_menu)
            invalidateMenu()
        }
    }

    override fun storeClicks(): Observable<Unit> = storeRelay

    override fun discussClicks(): Observable<Unit> = discussRelay

    override fun profileClicks(): Observable<Unit> = profileRelay

    override fun uploadClicks(): Observable<Unit> = uploadRelay

    override fun updateClicks(): Observable<Unit> = updateRelay

    override fun laterClicks(): Observable<Unit> = laterRelay

    override fun searchClicks(): Observable<Unit> = searchRelay

    override fun moderationClicks(): Observable<Unit> = moderationRelay

    override fun installedClicks(): Observable<Unit> = installedRelay

    override fun distroClicks(): Observable<Unit> = distroRelay

    override fun settingsClicks(): Observable<Unit> = settingsRelay

    override fun aboutClicks(): Observable<Unit> = aboutRelay

}
