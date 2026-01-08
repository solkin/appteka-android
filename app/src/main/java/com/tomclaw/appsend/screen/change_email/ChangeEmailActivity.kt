package com.tomclaw.appsend.screen.change_email

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.change_email.di.ChangeEmailModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject

class ChangeEmailActivity : AppCompatActivity(), ChangeEmailPresenter.ChangeEmailRouter {

    @Inject
    lateinit var presenter: ChangeEmailPresenter

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .changeEmailComponent(ChangeEmailModule(this, presenterState))
            .inject(activity = this)
        updateTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_email_activity)

        val view = ChangeEmailViewImpl(window.decorView)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-change-email-screen")
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

    override fun leaveScreen(success: Boolean) {
        if (success) {
            setResult(RESULT_OK)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

}

fun createChangeEmailActivityIntent(
    context: Context,
): Intent = Intent(context, ChangeEmailActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
