package com.tomclaw.appsend.screen.home

import android.view.MenuItem
import android.view.View
import android.widget.Button
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import com.tomclaw.appsend.util.hide
import com.tomclaw.appsend.util.show
import io.reactivex.rxjava3.core.Observable

interface HomeView {

    fun showStoreToolbar(canModerate: Boolean)
    fun showFeedToolbar()
    fun showDiscussToolbar()
    fun showProfileToolbar()

    fun selectStoreTab()
    fun selectDiscussTab()
    fun selectProfileTab()

    fun showUnreadBadge(count: Int)
    fun showFeedBadge(count: Int)
    fun hideUnreadBadge()
    fun hideFeedBadge()

    fun showUpdateBlock()
    fun hideUpdateBlock()

    fun showUploadButton()
    fun showPostButton()
    fun hideFabButtons()

    fun showStatusDialog(block: Boolean, title: String?, message: String)

    fun storeClicks(): Observable<Unit>
    fun feedClicks(): Observable<Unit>
    fun discussClicks(): Observable<Unit>
    fun profileClicks(): Observable<Unit>

    fun uploadClicks(): Observable<Unit>
    fun postClicks(): Observable<Unit>
    fun updateClicks(): Observable<Unit>
    fun laterClicks(): Observable<Unit>

    fun searchClicks(): Observable<Unit>
    fun moderationClicks(): Observable<Unit>
    fun profileShareClicks(): Observable<Unit>
    fun installedClicks(): Observable<Unit>
    fun distroClicks(): Observable<Unit>
    fun settingsClicks(): Observable<Unit>
    fun aboutClicks(): Observable<Unit>

    fun exitAppClicks(): Observable<Unit>
}

class HomeViewImpl(view: View) : HomeView {

    private val context = view.context

    private val toolbar: MaterialToolbar =
        view.findViewById(R.id.toolbar)

    private val bottomNavigation: BottomNavigationView =
        view.findViewById(R.id.bottom_navigation)

    private val updateBlock: View =
        view.findViewById(R.id.update_block)

    private val uploadButton: FloatingActionButton =
        view.findViewById(R.id.fab_upload)

    private val postButton: FloatingActionButton =
        view.findViewById(R.id.fab_post)

    private val updateButton: Button =
        view.findViewById(R.id.update)

    private val laterButton: Button =
        view.findViewById(R.id.update_later)

    private val storeRelay = PublishRelay.create<Unit>()
    private val feedRelay = PublishRelay.create<Unit>()
    private val discussRelay = PublishRelay.create<Unit>()
    private val profileRelay = PublishRelay.create<Unit>()
    private val uploadRelay = PublishRelay.create<Unit>()
    private val postRelay = PublishRelay.create<Unit>()
    private val updateRelay = PublishRelay.create<Unit>()
    private val laterRelay = PublishRelay.create<Unit>()
    private val searchRelay = PublishRelay.create<Unit>()
    private val moderationRelay = PublishRelay.create<Unit>()
    private val profileShareRelay = PublishRelay.create<Unit>()
    private val installedRelay = PublishRelay.create<Unit>()
    private val distroRelay = PublishRelay.create<Unit>()
    private val settingsRelay = PublishRelay.create<Unit>()
    private val aboutRelay = PublishRelay.create<Unit>()
    private val exitAppRelay = PublishRelay.create<Unit>()

    init {
        // 🔥 FAST & DETERMINISTIC STARTUP STATE
        toolbar.show()
        bottomNavigation.show()
        updateBlock.hide()
        hideFabButtons()

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_search -> searchRelay.accept(Unit)
                R.id.menu_moderation -> moderationRelay.accept(Unit)
                R.id.menu_share -> profileShareRelay.accept(Unit)
                R.id.menu_installed -> installedRelay.accept(Unit)
                R.id.menu_distro -> distroRelay.accept(Unit)
                R.id.menu_settings -> settingsRelay.accept(Unit)
                R.id.menu_about -> aboutRelay.accept(Unit)
            }
            true
        }

        bottomNavigation.setOnItemSelectedListener { item: MenuItem ->
            if (bottomNavigation.selectedItemId == item.itemId) {
                return@setOnItemSelectedListener true
            }
            when (item.itemId) {
                R.id.nav_store -> storeRelay.accept(Unit)
                R.id.nav_feed -> feedRelay.accept(Unit)
                R.id.nav_discuss -> discussRelay.accept(Unit)
                R.id.nav_profile -> profileRelay.accept(Unit)
            }
            true
        }

        bottomNavigation.setOnItemReselectedListener {
            // no-op (avoid duplicate work)
        }

        uploadButton.clicks(uploadRelay)
        postButton.clicks(postRelay)
        updateButton.clicks(updateRelay)
        laterButton.clicks(laterRelay)
    }

    override fun showStoreToolbar(canModerate: Boolean) {
        toolbar.apply {
            setTitle(R.string.tab_store)
            menu.clear()
            inflateMenu(R.menu.store_menu)
            if (!canModerate) {
                menu.removeItem(R.id.menu_moderation)
            }
        }
    }

    override fun showFeedToolbar() {
        toolbar.apply {
            setTitle(R.string.tab_feed)
            menu.clear()
            inflateMenu(R.menu.home_menu)
        }
    }

    override fun showDiscussToolbar() {
        toolbar.apply {
            setTitle(R.string.tab_discuss)
            menu.clear()
            inflateMenu(R.menu.home_menu)
        }
    }

    override fun showProfileToolbar() {
        toolbar.apply {
            setTitle(R.string.tab_profile)
            menu.clear()
            inflateMenu(R.menu.user_menu)
        }
    }

    override fun selectStoreTab() {
        if (bottomNavigation.selectedItemId != R.id.nav_store) {
            bottomNavigation.selectedItemId = R.id.nav_store
        }
    }

    override fun selectDiscussTab() {
        if (bottomNavigation.selectedItemId != R.id.nav_discuss) {
            bottomNavigation.selectedItemId = R.id.nav_discuss
        }
    }

    override fun selectProfileTab() {
        if (bottomNavigation.selectedItemId != R.id.nav_profile) {
            bottomNavigation.selectedItemId = R.id.nav_profile
        }
    }

    override fun showUnreadBadge(count: Int) {
        bottomNavigation.getOrCreateBadge(R.id.nav_discuss).apply {
            number = count
            isVisible = true
        }
    }

    override fun showFeedBadge(count: Int) {
        bottomNavigation.getOrCreateBadge(R.id.nav_feed).apply {
            number = count
            isVisible = true
        }
    }

    override fun hideUnreadBadge() {
        bottomNavigation.getBadge(R.id.nav_discuss)?.apply {
            clearNumber()
            isVisible = false
        }
    }

    override fun hideFeedBadge() {
        bottomNavigation.getBadge(R.id.nav_feed)?.apply {
            clearNumber()
            isVisible = false
        }
    }

    override fun showUpdateBlock() {
        updateBlock.show()
    }

    override fun hideUpdateBlock() {
        updateBlock.hide()
    }

    override fun showUploadButton() {
        uploadButton.show()
    }

    override fun showPostButton() {
        postButton.show()
    }

    override fun hideFabButtons() {
        uploadButton.hide()
        postButton.hide()
    }

    override fun showStatusDialog(block: Boolean, title: String?, message: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(!block)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (block) exitAppRelay.accept(Unit)
            }
            .show()
    }

    override fun storeClicks(): Observable<Unit> = storeRelay
    override fun feedClicks(): Observable<Unit> = feedRelay
    override fun discussClicks(): Observable<Unit> = discussRelay
    override fun profileClicks(): Observable<Unit> = profileRelay
    override fun uploadClicks(): Observable<Unit> = uploadRelay
    override fun postClicks(): Observable<Unit> = postRelay
    override fun updateClicks(): Observable<Unit> = updateRelay
    override fun laterClicks(): Observable<Unit> = laterRelay
    override fun searchClicks(): Observable<Unit> = searchRelay
    override fun moderationClicks(): Observable<Unit> = moderationRelay
    override fun profileShareClicks(): Observable<Unit> = profileShareRelay
    override fun installedClicks(): Observable<Unit> = installedRelay
    override fun distroClicks(): Observable<Unit> = distroRelay
    override fun settingsClicks(): Observable<Unit> = settingsRelay
    override fun aboutClicks(): Observable<Unit> = aboutRelay
    override fun exitAppClicks(): Observable<Unit> = exitAppRelay
}