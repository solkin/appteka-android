package com.tomclaw.appsend.screen.unlink

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.unlink.di.UnlinkModule
import javax.inject.Inject

class UnlinkActivity : AppCompatActivity(), UnlinkPresenter.UnlinkRouter {

    @Inject
    lateinit var presenter: UnlinkPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
                .unlinkComponent(UnlinkModule(this, presenterState))
                .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.unlink_activity)

        val view = UnlinkViewImpl(window.decorView)

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

private const val KEY_PRESENTER_STATE = "presenter_state"
