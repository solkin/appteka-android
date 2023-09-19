package com.tomclaw.appsend.screen.auth.verify_code

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.auth.verify_code.di.VerifyCodeModule
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class VerifyCodeActivity : AppCompatActivity(), VerifyCodePresenter.VerifyCodeRouter {

    @Inject
    lateinit var presenter: VerifyCodePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val email = intent.getStringExtra(EXTRA_EMAIL)
            ?: throw IllegalArgumentException("Email must be provided")
        val registered = intent.getBooleanExtra(EXTRA_REGISTERED, false)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .verifyCodeComponent(VerifyCodeModule(this, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.verify_code_activity)

        val view = VerifyCodeViewImpl(window.decorView)

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

    override fun leaveScreen(success: Boolean) {
        if (success) {
            setResult(RESULT_OK)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

}

fun createVerifyCodeActivityIntent(
    context: Context,
    email: String,
    registered: Boolean,
): Intent = Intent(context, VerifyCodeActivity::class.java)
    .putExtra(EXTRA_EMAIL, email)
    .putExtra(EXTRA_REGISTERED, registered)

private const val EXTRA_EMAIL = "email"
private const val EXTRA_REGISTERED = "registered"
private const val KEY_PRESENTER_STATE = "presenter_state"
