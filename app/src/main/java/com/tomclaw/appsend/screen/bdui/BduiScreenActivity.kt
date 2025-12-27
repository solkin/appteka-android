package com.tomclaw.appsend.screen.bdui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.about.createAboutActivityIntent
import com.tomclaw.appsend.screen.agreement.createAgreementActivityIntent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.bdui.di.BduiScreenModule
import com.tomclaw.appsend.screen.chat.createChatActivityIntent
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.distro.createDistroActivityIntent
import com.tomclaw.appsend.screen.downloads.createDownloadsActivityIntent
import com.tomclaw.appsend.screen.favorite.createFavoriteActivityIntent
import com.tomclaw.appsend.screen.feed.createFeedActivityIntent
import com.tomclaw.appsend.screen.home.createHomeActivityIntent
import com.tomclaw.appsend.screen.installed.createInstalledActivityIntent
import com.tomclaw.appsend.screen.moderation.createModerationActivityIntent
import com.tomclaw.appsend.screen.permissions.createPermissionsActivityIntent
import com.tomclaw.appsend.screen.post.createPostActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.ratings.createRatingsActivityIntent
import com.tomclaw.appsend.screen.reviews.createReviewsActivityIntent
import com.tomclaw.appsend.screen.search.createSearchActivityIntent
import com.tomclaw.appsend.screen.settings.createSettingsActivityIntent
import com.tomclaw.appsend.screen.subscriptions.Tab
import com.tomclaw.appsend.screen.subscriptions.createSubscriptionsActivityIntent
import com.tomclaw.appsend.screen.unlink.createUnlinkActivityIntent
import com.tomclaw.appsend.screen.unpublish.createUnpublishActivityIntent
import com.tomclaw.appsend.screen.uploads.createUploadsActivityIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject

/**
 * Universal BDUI Screen that loads and renders UI from a remote JSON schema.
 *
 * Usage:
 * ```kotlin
 * val intent = createBduiScreenActivityIntent(
 *     context = this,
 *     url = "https://api.example.com/bdui/screen.json",
 *     title = "Dynamic Screen"
 * )
 * startActivity(intent)
 * ```
 */
class BduiScreenActivity : AppCompatActivity(), BduiScreenPresenter.BduiScreenRouter {

    @Inject
    lateinit var presenter: BduiScreenPresenter

    @Inject
    lateinit var schedulersFactory: SchedulersFactory

    @Inject
    lateinit var preferencesStorage: com.tomclaw.appsend.util.bdui.BduiPreferencesStorage

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val url = intent.getStringExtra(EXTRA_URL)
            ?: throw IllegalArgumentException("URL must be provided")
        val title = intent.getStringExtra(EXTRA_TITLE)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .bduiScreenComponent(BduiScreenModule(url, title, presenterState))
            .inject(activity = this)
        updateTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.bdui_screen_activity)

        val view = BduiScreenViewImpl(window.decorView, schedulersFactory, preferencesStorage)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-bdui-screen")
        }
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

    override fun handleCallback(name: String, data: Any?) {
        // Handle standard callbacks
        when (name) {
            "close" -> finish()
            "back" -> onBackPressedDispatcher.onBackPressed()
            "setResult" -> {
                val resultData = data as? Map<*, *>
                val resultCode = (resultData?.get("code") as? Number)?.toInt() ?: RESULT_OK
                setResult(resultCode)
            }
            "finishWithResult" -> {
                val resultData = data as? Map<*, *>
                val resultCode = (resultData?.get("code") as? Number)?.toInt() ?: RESULT_OK
                setResult(resultCode)
                finish()
            }
            else -> {
                // Unknown callback - could be logged or handled by subclass
            }
        }
    }

    override fun handleRoute(screen: String, params: Map<String, Any>?) {
        val intent = createRouteIntent(screen, params) ?: return
        startActivity(intent)
    }

    override fun handleOpenUrl(url: String, external: Boolean) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun handleShare(text: String, title: String?) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooserTitle = title ?: getString(R.string.share)
        val chooser = Intent.createChooser(intent, chooserTitle)
        startActivity(chooser)
    }

    private fun createRouteIntent(screen: String, params: Map<String, Any>?): Intent? {
        return when (screen) {
            SCREEN_HOME -> createHomeActivityIntent(this)

            SCREEN_DISTRO -> createDistroActivityIntent(this)

            SCREEN_INSTALLED -> createInstalledActivityIntent(
                context = this,
                picker = params?.getBoolean("picker") ?: false
            )

            SCREEN_DETAILS -> createDetailsActivityIntent(
                context = this,
                appId = params?.getString("appId"),
                packageName = params?.getString("packageName"),
                label = params?.getString("label") ?: "",
                moderation = params?.getBoolean("moderation") ?: false,
                finishOnly = params?.getBoolean("finishOnly") ?: false
            )

            SCREEN_CHAT -> {
                val topicId = params?.getInt("topicId") ?: return null
                createChatActivityIntent(
                    context = this,
                    topicId = topicId,
                    title = params.getString("title")
                )
            }

            SCREEN_SEARCH -> createSearchActivityIntent(this)

            SCREEN_UNPUBLISH -> {
                val appId = params?.getString("appId") ?: return null
                createUnpublishActivityIntent(
                    context = this,
                    appId = appId,
                    label = params.getString("label")
                )
            }

            SCREEN_UNLINK -> {
                val appId = params?.getString("appId") ?: return null
                createUnlinkActivityIntent(
                    context = this,
                    appId = appId,
                    label = params.getString("label")
                )
            }

            SCREEN_PROFILE -> {
                val userId = params?.getInt("userId") ?: return null
                createProfileActivityIntent(this, userId)
            }

            SCREEN_REQUEST_CODE -> createRequestCodeActivityIntent(this)

            SCREEN_FEED -> {
                val userId = params?.getInt("userId") ?: return null
                createFeedActivityIntent(this, userId)
            }

            SCREEN_AGREEMENT -> createAgreementActivityIntent(this)

            SCREEN_POST -> createPostActivityIntent(this)

            SCREEN_MODERATION -> createModerationActivityIntent(this)

            SCREEN_PERMISSIONS -> {
                val permissions = params?.getStringList("permissions") ?: return null
                createPermissionsActivityIntent(this, permissions)
            }

            SCREEN_FAVORITE -> {
                val userId = params?.getInt("userId") ?: return null
                createFavoriteActivityIntent(this, userId)
            }

            SCREEN_RATINGS -> {
                val appId = params?.getString("appId") ?: return null
                createRatingsActivityIntent(this, appId)
            }

            SCREEN_UPLOADS -> {
                val userId = params?.getInt("userId") ?: return null
                createUploadsActivityIntent(this, userId)
            }

            SCREEN_DOWNLOADS -> {
                val userId = params?.getInt("userId") ?: return null
                createDownloadsActivityIntent(this, userId)
            }

            SCREEN_SUBSCRIPTIONS -> {
                val userId = params?.getInt("userId") ?: return null
                val tabName = params.getString("tab") ?: "subscribers"
                val tab = try {
                    Tab.valueOf(tabName.uppercase())
                } catch (e: IllegalArgumentException) {
                    Tab.SUBSCRIBERS
                }
                createSubscriptionsActivityIntent(this, userId, tab)
            }

            SCREEN_REVIEWS -> {
                val userId = params?.getInt("userId") ?: return null
                createReviewsActivityIntent(this, userId)
            }

            SCREEN_ABOUT -> createAboutActivityIntent(this)

            SCREEN_SETTINGS -> createSettingsActivityIntent(this)

            SCREEN_BDUI -> {
                val url = params?.getString("url") ?: return null
                createBduiScreenActivityIntent(
                    context = this,
                    url = url,
                    title = params.getString("title")
                )
            }

            else -> null
        }
    }

    private fun Map<String, Any>.getString(key: String): String? {
        return this[key]?.toString()
    }

    private fun Map<String, Any>.getInt(key: String): Int? {
        return when (val value = this[key]) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    private fun Map<String, Any>.getBoolean(key: String): Boolean {
        return when (val value = this[key]) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.getStringList(key: String): List<String>? {
        return when (val value = this[key]) {
            is List<*> -> value.filterIsInstance<String>()
            else -> null
        }
    }

    companion object {
        const val SCREEN_HOME = "home"
        const val SCREEN_DISTRO = "distro"
        const val SCREEN_INSTALLED = "installed"
        const val SCREEN_DETAILS = "details"
        const val SCREEN_CHAT = "chat"
        const val SCREEN_SEARCH = "search"
        const val SCREEN_UNPUBLISH = "unpublish"
        const val SCREEN_UNLINK = "unlink"
        const val SCREEN_PROFILE = "profile"
        const val SCREEN_REQUEST_CODE = "request_code"
        const val SCREEN_FEED = "feed"
        const val SCREEN_AGREEMENT = "agreement"
        const val SCREEN_POST = "post"
        const val SCREEN_MODERATION = "moderation"
        const val SCREEN_PERMISSIONS = "permissions"
        const val SCREEN_FAVORITE = "favorite"
        const val SCREEN_RATINGS = "ratings"
        const val SCREEN_UPLOADS = "uploads"
        const val SCREEN_DOWNLOADS = "downloads"
        const val SCREEN_SUBSCRIPTIONS = "subscriptions"
        const val SCREEN_REVIEWS = "reviews"
        const val SCREEN_ABOUT = "about"
        const val SCREEN_SETTINGS = "settings"
        const val SCREEN_BDUI = "bdui"
    }

}

/**
 * Creates an Intent to open the BDUI Screen.
 *
 * @param context Context
 * @param url URL to load the BDUI JSON schema from
 * @param title Optional title for the toolbar
 * @return Intent to start BduiScreenActivity
 */
fun createBduiScreenActivityIntent(
    context: Context,
    url: String,
    title: String? = null,
): Intent = Intent(context, BduiScreenActivity::class.java)
    .putExtra(EXTRA_URL, url)
    .putExtra(EXTRA_TITLE, title)

private const val EXTRA_URL = "url"
private const val EXTRA_TITLE = "title"
private const val KEY_PRESENTER_STATE = "presenter_state"

