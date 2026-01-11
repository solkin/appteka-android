package com.tomclaw.appsend.screen.unpublish

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.unpublish.di.UnpublishModule
import javax.inject.Inject

class UnpublishActivity : AppCompatActivity(), UnpublishPresenter.UnpublishRouter {

    @Inject
    lateinit var presenter: UnpublishPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("App ID must be provided")
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
                .unpublishComponent(UnpublishModule(this, appId, presenterState))
                .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.unpublish_activity)

        val view = UnpublishViewImpl(window.decorView, title = label)

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
        val result = when(success) {
            true -> RESULT_OK
            else -> RESULT_CANCELED
        }
        setResult(result)
        finish()
    }

}

fun createUnpublishActivityIntent(
    context: Context,
    appId: String,
    label: String?,
): Intent = Intent(context, UnpublishActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_LABEL, label)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_LABEL = "label"

private const val KEY_PRESENTER_STATE = "presenter_state"
