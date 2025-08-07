package com.tomclaw.appsend.screen.unlink

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.auth.verify_code.VerifyCodeActivity
import com.tomclaw.appsend.screen.unlink.di.UnlinkModule
import javax.inject.Inject

class UnlinkActivity : AppCompatActivity(), UnlinkPresenter.UnlinkRouter {

    @Inject
    lateinit var presenter: UnlinkPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("App ID must be provided")
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
                .unlinkComponent(UnlinkModule(this, appId, presenterState))
                .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.unlink_activity)

        val view = UnlinkViewImpl(window.decorView, title = label)

        presenter.attachView(view)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.onBackPressed()
            }
        })
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

fun createUnlinkActivityIntent(
    context: Context,
    appId: String,
    label: String,
): Intent = Intent(context, VerifyCodeActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_LABEL, label)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_LABEL = "label"

private const val KEY_PRESENTER_STATE = "presenter_state"
