package com.tomclaw.appsend.screen.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
// import com.tomclaw.appsend.util.restartIfThemeChanged
// import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject
import kotlin.system.exitProcess

class HomeActivity : AppCompatActivity(), HomePresenter.HomeRouter {

    @Inject
    lateinit var presenter: HomePresenter

    @Inject
    lateinit var analytics: Analytics

    private var isDarkTheme: Boolean = false
    private val handler: Handler = Handler(Looper.getMainLooper())

    private val postLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == RESULT_OK) {
                    invalidateFragment(result.data)
                }
            } catch (t: Throwable) {
                // swallow to avoid crash from activity result handling
                Log.w("HomeActivity", "postLauncher handling failed", t)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // protect injection and theme update
        try {
            val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
            Appteka.getComponent()
                .homeComponent(HomeModule(context = this, startAction = intent.action, presenterState))
                .inject(activity = this)
        } catch (t: Throwable) {
            // swallow injection error to avoid crashing the activity
            Log.w("HomeActivity", "DI injection failed", t)
        }

        /* try {
            isDarkTheme = updateTheme()
        } catch (t: Throwable) {
            Log.w("HomeActivity", "updateTheme failed", t)
            isDarkTheme = false
        } */

        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.home_activity)
        } catch (t: Throwable) {
            // if layout inflate fails, log and continue (avoid crash)
            Log.e("HomeActivity", "setContentView failed", t)
        }

        try {
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    try {
                        presenter.onBackPressed()
                    } catch (t: Throwable) {
                        Log.w("HomeActivity", "presenter.onBackPressed failed", t)
                        // fallback: finish activity
                        try {
                            finish()
                        } catch (_: Throwable) { /* no-op */ }
                    }
                }
            })
        } catch (t: Throwable) {
            Log.w("HomeActivity", "onBackPressedDispatcher callback failed", t)
        }

        try {
            val view = HomeViewImpl(window.decorView)
            presenter.attachView(view)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "attachView failed", t)
        }

        try {
            if (savedInstanceState == null) {
                analytics.trackEvent("open-home-screen")
            }
        } catch (t: Throwable) {
            Log.w("HomeActivity", "analytics.trackEvent failed", t)
        }
    }

    override fun showStoreFragment() {
        val fragment = getOrCreateFragment(INDEX_STORE) { createStoreFragment() }
        replaceFragment(fragment, INDEX_STORE)
    }

    override fun showFeedFragment() {
        val fragment = getOrCreateFragment(INDEX_FEED) { createFeedFragment() }
        replaceFragment(fragment, INDEX_FEED)
    }

    override fun showTopicsFragment() {
        val fragment = getOrCreateFragment(INDEX_DISCUSS) { createTopicsFragment() }
        replaceFragment(fragment, INDEX_DISCUSS)
    }

    override fun showProfileFragment() {
        val fragment = getOrCreateFragment(INDEX_PROFILE) { createProfileFragment() }
        replaceFragment(fragment, INDEX_PROFILE)
    }

    fun invalidateFragment(data: Intent?) {
        val pendingRunnable = Runnable {
            try {
                val fragment = supportFragmentManager.findFragmentById(R.id.frame) as? HomeFragment
                fragment?.handleEvent(data)
            } catch (t: Throwable) {
                Log.w("HomeActivity", "invalidateFragment handler failed", t)
            }
        }
        try {
            handler.post(pendingRunnable)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "handler.post failed in invalidateFragment", t)
        }
    }

    override fun openUploadScreen() {
        try {
            val intent = createUploadActivityIntent(context = this, pkg = null, apk = null, info = null)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openUploadScreen failed", t)
        }
    }

    override fun openPostScreen() {
        try {
            val intent = createPostActivityIntent(this)
            postLauncher.launch(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openPostScreen failed", t)
        }
    }

    override fun openSearchScreen() {
        try {
            val intent = createSearchActivityIntent(this)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openSearchScreen failed", t)
        }
    }

    override fun openModerationScreen() {
        try {
            val intent = createModerationActivityIntent(this)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openModerationScreen failed", t)
        }
    }

    override fun openInstalledScreen() {
        try {
            val intent = createInstalledActivityIntent(this)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openInstalledScreen failed", t)
        }
    }

    override fun openDistroScreen() {
        try {
            val intent = createDistroActivityIntent(this)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openDistroScreen failed", t)
        }
    }

    override fun openSettingsScreen() {
        try {
            val intent = createSettingsActivityIntent(this)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openSettingsScreen failed", t)
        }
    }

    override fun openAboutScreen() {
        try {
            val intent = createAboutActivityIntent(this)
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openAboutScreen failed", t)
        }
    }

    private fun getOrCreateFragment(index: Int, creator: () -> Fragment): Fragment {
        return try {
            supportFragmentManager.findFragmentByTag("fragment$index") ?: creator.invoke()
        } catch (t: Throwable) {
            Log.w("HomeActivity", "getOrCreateFragment failed", t)
            // fallback: try to create fragment anyway
            return try {
                creator.invoke()
            } catch (e: Throwable) {
                // last resort: empty Fragment to avoid crash
                Log.e("HomeActivity", "creator.invoke also failed, returning empty Fragment", e)
                Fragment()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, index: Int) {
        val pendingRunnable = Runnable {
            try {
                val fm = supportFragmentManager
                // If the state is already saved, use commitAllowingStateLoss to avoid IllegalStateException
                val transaction = fm.beginTransaction()
                    .setCustomAnimations(0, 0)
                    .replace(R.id.frame, fragment, "fragment$index")
                if (!fm.isStateSaved) {
                    transaction.commit()
                } else {
                    // commitAllowingStateLoss is used as a safe fallback to prevent crash
                    transaction.commitAllowingStateLoss()
                }
            } catch (t: Throwable) {
                // swallow to avoid crash — log for debugging
                Log.w("HomeActivity", "replaceFragment failed", t)
            }
        }
        try {
            handler.post(pendingRunnable)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "handler.post failed in replaceFragment", t)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            presenter.attachRouter(this)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "attachRouter failed", t)
        }
    }

    override fun onStop() {
        try {
            presenter.detachRouter()
        } catch (t: Throwable) {
            Log.w("HomeActivity", "detachRouter failed", t)
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        /* try {
            restartIfThemeChanged(isDarkTheme)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "restartIfThemeChanged failed", t)
        } */
    }

    override fun onDestroy() {
        try {
            presenter.detachView()
        } catch (t: Throwable) {
            Log.w("HomeActivity", "detachView failed", t)
        }
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        try {
            super.onSaveInstanceState(outState)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "super.onSaveInstanceState failed", t)
        }
        try {
            outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
        } catch (t: Throwable) {
            Log.w("HomeActivity", "saving presenter state failed", t)
        }
    }

    override fun openAppScreen(appId: String, title: String) {
        try {
            val intent = createDetailsActivityIntent(
                context = this,
                appId = appId,
                label = title,
                moderation = false,
                finishOnly = true
            )
            startActivity(intent)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openAppScreen failed", t)
        }
    }

    override fun openShareUrlDialog(text: String) {
        try {
            val intent = Intent().apply {
                setAction(Intent.ACTION_SEND)
                putExtra(Intent.EXTRA_TEXT, text)
                setType("text/plain")
            }
            val chooser = Intent.createChooser(intent, resources.getText(R.string.send_url_to))
            startActivity(chooser)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "openShareUrlDialog failed", t)
        }
    }

    override fun leaveScreen() {
        try {
            finish()
        } catch (t: Throwable) {
            Log.w("HomeActivity", "finish failed", t)
        }
    }

    override fun exitApp() {
        try {
            exitProcess(0)
        } catch (t: Throwable) {
            Log.w("HomeActivity", "exitProcess failed", t)
        }
    }

}

fun createHomeActivityIntent(
    context: Context,
): Intent = Intent(context, HomeActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val INDEX_STORE = 0
private const val INDEX_FEED = 1
private const val INDEX_DISCUSS = 2
private const val INDEX_PROFILE = 3