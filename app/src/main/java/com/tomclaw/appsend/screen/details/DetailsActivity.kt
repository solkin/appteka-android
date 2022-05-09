package com.tomclaw.appsend.screen.details

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.di.DetailsModule
import javax.inject.Inject

class DetailsActivity : AppCompatActivity(), DetailsPresenter.DetailsRouter {

    @Inject
    lateinit var presenter: DetailsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
                .detailsComponent(DetailsModule(this, presenterState))
                .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_activity)

        val view = DetailsViewImpl(window.decorView)

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

private const val KEY_PRESENTER_STATE = "presenter_state"
