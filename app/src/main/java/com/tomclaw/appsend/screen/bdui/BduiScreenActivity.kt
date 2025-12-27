package com.tomclaw.appsend.screen.bdui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.bdui.di.BduiScreenModule
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

        val view = BduiScreenViewImpl(window.decorView, schedulersFactory)

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

