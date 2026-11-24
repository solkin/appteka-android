package com.tomclaw.appsend.screen.ratings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.RatingScope
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.ratings.di.RatingsModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.ZipParcelable
import com.tomclaw.appsend.util.getParcelableCompat
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
        val hasBuildScope = intent.getBooleanExtra(EXTRA_HAS_BUILD_SCOPE, false)
        val initialScope = RatingScope.parse(intent.getStringExtra(EXTRA_INITIAL_SCOPE))
        val versionCode = intent.getIntExtra(EXTRA_VERSION_CODE, 0)
            .takeIf { intent.hasExtra(EXTRA_VERSION_CODE) }
        val versionName = intent.getStringExtra(EXTRA_VERSION_NAME).orEmpty()
        val presenterState = savedInstanceState
            ?.getParcelableCompat(KEY_PRESENTER_STATE, ZipParcelable::class.java)
            ?.restore<Bundle>()
        appComponent
            .ratingsComponent(
                RatingsModule(
                    context = this,
                    appId = appId,
                    hasBuildScope = hasBuildScope,
                    initialScope = initialScope,
                    versionCode = versionCode,
                    versionName = versionName,
                    state = presenterState,
                )
            )
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.ratings_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = RatingsViewImpl(window.decorView, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-ratings-screen")
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
        outState.putParcelable(KEY_PRESENTER_STATE, ZipParcelable(presenter.saveState()))
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
    appId: String,
    hasBuildScope: Boolean = false,
    initialScope: RatingScope = RatingScope.EDITION,
    versionCode: Int? = null,
    versionName: String? = null,
): Intent = Intent(context, RatingsActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_HAS_BUILD_SCOPE, hasBuildScope)
    .putExtra(EXTRA_INITIAL_SCOPE, initialScope.name)
    .apply { versionCode?.let { putExtra(EXTRA_VERSION_CODE, it) } }
    .putExtra(EXTRA_VERSION_NAME, versionName)

private const val EXTRA_APP_ID = "user_id"
private const val EXTRA_HAS_BUILD_SCOPE = "has_build_scope"
private const val EXTRA_INITIAL_SCOPE = "initial_scope"
private const val EXTRA_VERSION_CODE = "version_code"
private const val EXTRA_VERSION_NAME = "version_name"
private const val KEY_PRESENTER_STATE = "presenter_state"
