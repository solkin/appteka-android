package com.tomclaw.appsend.screen.auth.request_code

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.auth.request_code.di.RequestCodeModule
import com.tomclaw.appsend.screen.auth.verify_code.createVerifyCodeActivityIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class RequestCodeActivity : AppCompatActivity(), RequestCodePresenter.RequestCodeRouter {

    @Inject
    lateinit var presenter: RequestCodePresenter

    @Inject
    lateinit var analytics: Analytics

    private val verifyCodeResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                leaveScreen(true)
            }
        }

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

        if (savedInstanceState == null) {
            analytics.trackEvent("open-request-code-screen")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
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

    override fun showVerifyCodeScreen(
        email: String,
        requestId: String,
        registered: Boolean,
        codeRegex: String,
        nameRegex: String
    ) {
        val intent = createVerifyCodeActivityIntent(context = this, email, requestId, registered, codeRegex, nameRegex)
        verifyCodeResultLauncher.launch(intent)
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

fun createRequestCodeActivityIntent(
    context: Context,
): Intent = Intent(context, RequestCodeActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
