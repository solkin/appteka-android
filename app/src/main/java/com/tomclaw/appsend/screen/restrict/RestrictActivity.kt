package com.tomclaw.appsend.screen.restrict

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.screen.restrict.di.RestrictModule
import javax.inject.Inject

/**
 * One screen for taking an app off the store, two ways in.
 *
 * Blocking and unpublishing are separate actions with separate rights
 * and separate consequences, so they keep separate entry points — and
 * the mode they carry picks the components the screen runs on. Nothing
 * below the DI module branches on it.
 */
class RestrictActivity : AppCompatActivity(), RestrictRouter {

    @Inject
    lateinit var presenter: RestrictPresenter

    @Inject
    lateinit var resourceProvider: RestrictResourceProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("App ID must be provided")
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val mode = RestrictMode.valueOf(
            intent.getStringExtra(EXTRA_MODE) ?: RestrictMode.UNPUBLISH.name
        )

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .restrictComponent(RestrictModule(this, appId, mode, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.restrict_activity)

        val view = RestrictViewImpl(window.decorView, title = resourceProvider.title(label))

        presenter.attachView(view)
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

    override fun leaveScreen(success: Boolean) {
        val result = when (success) {
            true -> RESULT_OK
            else -> RESULT_CANCELED
        }
        setResult(result)
        finish()
    }

}

/** Which way an app is being taken off the store. */
enum class RestrictMode {
    UNPUBLISH,
    BLOCK,
}

fun createUnlinkActivityIntent(
    context: Context,
    appId: String,
    label: String?,
): Intent = createRestrictActivityIntent(context, appId, label, RestrictMode.BLOCK)

fun createUnpublishActivityIntent(
    context: Context,
    appId: String,
    label: String?,
): Intent = createRestrictActivityIntent(context, appId, label, RestrictMode.UNPUBLISH)

private fun createRestrictActivityIntent(
    context: Context,
    appId: String,
    label: String?,
    mode: RestrictMode,
): Intent = Intent(context, RestrictActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_MODE, mode.name)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_LABEL = "label"
private const val EXTRA_MODE = "mode"

private const val KEY_PRESENTER_STATE = "presenter_state"
