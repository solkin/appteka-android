package com.tomclaw.appsend.screen.auth.request_code

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.auth.request_code.di.RequestCodeModule
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class RequestCodeActivity : AppCompatActivity(), RequestCodePresenter.RequestCodeRouter {

    @Inject
    lateinit var presenter: RequestCodePresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .requestCodeComponent(RequestCodeModule(this, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_code_activity)

        val view = RequestCodeViewImpl(window.decorView)

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
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

}

fun createRequestCodeActivityIntent(
    context: Context,
): Intent = Intent(context, RequestCodeActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
