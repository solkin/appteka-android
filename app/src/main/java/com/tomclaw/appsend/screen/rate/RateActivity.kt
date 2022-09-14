package com.tomclaw.appsend.screen.rate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.rate.di.RateModule
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class RateActivity : AppCompatActivity(), RatePresenter.RateRouter {

    @Inject
    lateinit var presenter: RatePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("App ID must be provided")
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val icon = intent.getStringExtra(EXTRA_ICON)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .rateComponent(RateModule(this, appId, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.rate_activity)

        val view = RateViewImpl(window.decorView).apply {
            setTitle(label)
            setIcon(icon)
        }

        presenter.attachView(view)
    }

    override fun onBackPressed() {
        presenter.onBackPressed()
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

fun createRateActivityIntent(
    context: Context,
    appId: String,
    label: String? = null,
    icon: String? = null,
): Intent = Intent(context, RateActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_ICON, icon)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_LABEL = "label"
private const val EXTRA_ICON = "icon"
private const val KEY_PRESENTER_STATE = "presenter_state"
