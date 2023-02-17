package com.tomclaw.appsend.screen.upload

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.rate.RateActivity
import com.tomclaw.appsend.screen.upload.di.UploadModule
import javax.inject.Inject

class UploadActivity : AppCompatActivity(), UploadPresenter.UploadRouter {

    @Inject
    lateinit var presenter: UploadPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val info = intent.getParcelableExtra<PackageInfo?>(EXTRA_PACKAGE_INFO)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .uploadComponent(UploadModule(this, info, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_activity)

        val view = UploadViewImpl(window.decorView)

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

fun createUploadActivityIntent(
    context: Context,
    info: PackageInfo?,
): Intent = Intent(context, UploadActivity::class.java)
    .putExtra(EXTRA_PACKAGE_INFO, info)

private const val EXTRA_PACKAGE_INFO = "package_info"
private const val KEY_PRESENTER_STATE = "presenter_state"
