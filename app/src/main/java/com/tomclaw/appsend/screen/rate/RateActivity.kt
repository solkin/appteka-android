package com.tomclaw.appsend.screen.rate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.rate.di.RateModule
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.getParcelableExtraCompat
import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject

class RateActivity : AppCompatActivity(), RatePresenter.RateRouter {

    @Inject
    lateinit var presenter: RatePresenter

    @Inject
    lateinit var analytics: Analytics

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
            ?: throw IllegalArgumentException("App ID must be provided")
        val userBrief = intent.getParcelableExtraCompat(EXTRA_USER_BRIEF, UserBrief::class.java)
            ?: throw IllegalArgumentException("User brief must be provided")
        val label = intent.getStringExtra(EXTRA_LABEL).orEmpty()
        val icon = intent.getStringExtra(EXTRA_ICON)
        val rating = intent.getFloatExtra(EXTRA_RATING, 0f)
        val review = intent.getStringExtra(EXTRA_REVIEW).orEmpty()

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .rateComponent(RateModule(this, appId, userBrief, rating, review, presenterState))
            .inject(activity = this)
        updateTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.rate_activity)

        val view = RateViewImpl(window.decorView).apply {
            setTitle(label)
            setIcon(icon)
        }

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-rate-screen")
        }

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

    override fun leaveScreen(success: Boolean) {
        if (success) {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

}

fun createRateActivityIntent(
    context: Context,
    appId: String,
    userBrief: UserBrief,
    rating: Float? = null,
    review: String? = null,
    label: String? = null,
    icon: String? = null
): Intent = Intent(context, RateActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_USER_BRIEF, userBrief)
    .putExtra(EXTRA_RATING, rating)
    .putExtra(EXTRA_REVIEW, review)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_ICON, icon)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_USER_BRIEF = "user_brief"
private const val EXTRA_LABEL = "label"
private const val EXTRA_ICON = "icon"
private const val EXTRA_RATING = "rating"
private const val EXTRA_REVIEW = "review"
private const val KEY_PRESENTER_STATE = "presenter_state"
