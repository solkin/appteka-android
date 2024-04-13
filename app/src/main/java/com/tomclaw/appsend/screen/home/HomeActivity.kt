package com.tomclaw.appsend.screen.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.about.AboutActivity.createAboutActivityIntent
import com.tomclaw.appsend.main.local.DistroActivity.createDistroActivityIntent
import com.tomclaw.appsend.main.local.InstalledActivity.createInstalledActivityIntent
import com.tomclaw.appsend.main.settings.SettingsActivity.createSettingsActivityIntent
import com.tomclaw.appsend.main.store.search.SearchActivity.createSearchActivityIntent
import com.tomclaw.appsend.screen.home.di.HomeModule
import com.tomclaw.appsend.screen.moderation.createModerationActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileFragment
import com.tomclaw.appsend.screen.store.createStoreFragment
import com.tomclaw.appsend.screen.topics.createTopicsFragment
import com.tomclaw.appsend.screen.upload.createUploadActivityIntent
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class HomeActivity : AppCompatActivity(), HomePresenter.HomeRouter {

    @Inject
    lateinit var presenter: HomePresenter

    private val handler: Handler = Handler(Looper.getMainLooper())
    private val fragments = Array<Fragment?>(size = 3) { null }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .homeComponent(HomeModule(this, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                leaveScreen()
            }
        })

        val view = HomeViewImpl(window.decorView)

        presenter.attachView(view)
    }

    override fun showStoreFragment() {
        val fragment = getOrCreateFragment(INDEX_STORE) { createStoreFragment() }
        replaceFragment(fragment, INDEX_STORE)
    }

    override fun showTopicsFragment() {
        val fragment = getOrCreateFragment(INDEX_DISCUSS) { createTopicsFragment() }
        replaceFragment(fragment, INDEX_DISCUSS)
    }

    override fun showProfileFragment() {
        val fragment = getOrCreateFragment(INDEX_PROFILE) { createProfileFragment() }
        replaceFragment(fragment, INDEX_PROFILE)
    }

    override fun openUploadScreen() {
        val intent = createUploadActivityIntent(context = this, pkg = null, apk = null, info = null)
        startActivity(intent)
    }

    override fun openSearchScreen() {
        val intent = createSearchActivityIntent(this)
        startActivity(intent)
    }

    override fun openModerationScreen() {
        val intent = createModerationActivityIntent(this)
        startActivity(intent)
    }

    override fun openInstalledScreen() {
        val intent = createInstalledActivityIntent(this)
        startActivity(intent)
    }

    override fun openDistroScreen() {
        val intent = createDistroActivityIntent(this)
        startActivity(intent)
    }

    override fun openSettingsScreen() {
        val intent = createSettingsActivityIntent(this)
        startActivity(intent)
    }

    override fun openAboutScreen() {
        val intent = createAboutActivityIntent(this)
        startActivity(intent)
    }

    private fun getOrCreateFragment(index: Int, creator: () -> Fragment): Fragment {
        return fragments[index] ?: let {
            val fragment = creator.invoke()
            fragments[index] = fragment
            fragment
        }
    }

    private fun replaceFragment(fragment: Fragment, index: Int) {
        val pendingRunnable = Runnable {
            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(0, 0)
                .replace(R.id.frame, fragment, "fragment$index")
                .commitAllowingStateLoss()
        }
        handler.post(pendingRunnable)
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

    override fun leaveScreen() {
        finish()
    }

}

fun createHomeActivityIntent(
    context: Context,
): Intent = Intent(context, HomeActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val INDEX_STORE = 0
private const val INDEX_DISCUSS = 1
private const val INDEX_PROFILE = 2