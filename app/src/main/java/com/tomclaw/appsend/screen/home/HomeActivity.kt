package com.tomclaw.appsend.screen.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.settings.createSettingsActivityIntent
import com.tomclaw.appsend.screen.search.createSearchActivityIntent
import com.tomclaw.appsend.screen.about.createAboutActivityIntent
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.distro.createDistroActivityIntent
import com.tomclaw.appsend.screen.feed.createFeedFragment
import com.tomclaw.appsend.screen.home.di.HomeModule
import com.tomclaw.appsend.screen.installed.createInstalledActivityIntent
import com.tomclaw.appsend.screen.moderation.createModerationActivityIntent
import com.tomclaw.appsend.screen.post.createPostActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileFragment
import com.tomclaw.appsend.screen.store.createStoreFragment
import com.tomclaw.appsend.screen.topics.createTopicsFragment
import com.tomclaw.appsend.screen.upload.createUploadActivityIntent
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity(), HomePresenter.HomeRouter {

    @Inject
    lateinit var presenter: HomePresenter

    @Inject
    lateinit var analytics: Analytics

    private val postLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                invalidateFragment(result.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)

        Appteka.getComponent()
            .homeComponent(HomeModule(this, intent.action, presenterState))
            .inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    presenter.onBackPressed()
                }
            }
        )

        presenter.attachView(HomeViewImpl(window.decorView))

        if (savedInstanceState == null) {
            analytics.trackEvent("open-home-screen")
        }
    }

    // ---------------- fragments ----------------

    override fun showStoreFragment() {
        replaceFragment(INDEX_STORE) { createStoreFragment() }
    }

    override fun showFeedFragment() {
        replaceFragment(INDEX_FEED) { createFeedFragment() }
    }

    override fun showTopicsFragment() {
        replaceFragment(INDEX_DISCUSS) { createTopicsFragment() }
    }

    override fun showProfileFragment() {
        replaceFragment(INDEX_PROFILE) { createProfileFragment() }
    }

    private fun replaceFragment(index: Int, creator: () -> Fragment) {
        val tag = "fragment$index"
        val fragment = creator() // 🔥 ALWAYS FRESH INSTANCE

        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.frame, fragment, tag)
        }
    }

    fun invalidateFragment(data: Intent?) {
        val fragment = supportFragmentManager.findFragmentById(R.id.frame) as? HomeFragment
        fragment?.handleEvent(data)
    }

    // ---------------- navigation ----------------

    override fun openUploadScreen() {
        startActivity(createUploadActivityIntent(this, null, null, null))
    }

    override fun openPostScreen() {
        postLauncher.launch(createPostActivityIntent(this))
    }

    override fun openSearchScreen() {
        startActivity(createSearchActivityIntent(this))
    }

    override fun openModerationScreen() {
        startActivity(createModerationActivityIntent(this))
    }

    override fun openInstalledScreen() {
        startActivity(createInstalledActivityIntent(this))
    }

    override fun openDistroScreen() {
        startActivity(createDistroActivityIntent(this))
    }

    override fun openSettingsScreen() {
        startActivity(createSettingsActivityIntent(this))
    }

    override fun openAboutScreen() {
        startActivity(createAboutActivityIntent(this))
    }

    override fun openAppScreen(appId: String, title: String) {
        startActivity(
            createDetailsActivityIntent(
                context = this,
                appId = appId,
                label = title,
                moderation = false,
                finishOnly = true
            )
        )
    }

    override fun openShareUrlDialog(text: String) {
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                },
                getText(R.string.send_url_to)
            )
        )
    }

    override fun leaveScreen() {
        finish()
    }

    override fun exitApp() {
        exitProcess(0)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }
}

fun createHomeActivityIntent(context: Context): Intent =
    Intent(context, HomeActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val INDEX_STORE = 0
private const val INDEX_FEED = 1
private const val INDEX_DISCUSS = 2
private const val INDEX_PROFILE = 3