package com.tomclaw.appsend.screen.ratings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.ratings.di.RatingsModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class RatingsActivity : AppCompatActivity(), RatingsPresenter.RatingsRouter {

    @Inject
    lateinit var presenter: RatingsPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("App ID must be provided")
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .ratingsComponent(RatingsModule(this, appId, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.ratings_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = RatingsViewImpl(window.decorView, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-ratings-screen")
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

    override fun openUserProfile(userId: Int) {
        val intent = createProfileActivityIntent(
            context = this,
            userId
        )
        startActivity(intent)
    }

    override fun leaveScreen() {
        finish()
    }

}

fun createRatingsActivityIntent(
    context: Context,
    appId: String
): Intent = Intent(context, RatingsActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)

private const val EXTRA_APP_ID = "user_id"
private const val KEY_PRESENTER_STATE = "presenter_state"
