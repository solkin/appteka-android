package com.tomclaw.appsend.screen.subscriptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.subscribers.createSubscribersFragment
import com.tomclaw.appsend.screen.subscriptions.di.SubscriptionsModule
import com.tomclaw.appsend.util.ThemeHelper
import javax.inject.Inject

class SubscriptionsActivity : AppCompatActivity(), SubscriptionsPresenter.SubscriptionsRouter {

    @Inject
    lateinit var presenter: SubscriptionsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0).takeIf { it != 0 }
            ?: throw IllegalArgumentException("user ID must be provided")
        val activeTab = intent.getIntExtra(EXTRA_ACTIVE_TAB, Tab.SUBSCRIBERS.index)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .subscriptionsComponent(SubscriptionsModule(userId, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.subscriptions_activity)

        val adapter = SubscriptionsAdapter(supportFragmentManager, lifecycle, userId)
        val view = SubscriptionsViewImpl(window.decorView, adapter)

        presenter.attachView(view)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                leaveScreen()
            }
        })
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

    override fun leaveScreen() {
        finish()
    }

}

enum class Tab(val index: Int) {
    SUBSCRIBERS(index = 0),
    PUBLISHERS(index = 1),
}

fun createSubscriptionsActivityIntent(
    context: Context,
    userId: Int,
    activeTab: Tab,
): Intent = Intent(context, SubscriptionsActivity::class.java)
    .putExtra(EXTRA_USER_ID, userId)
    .putExtra(EXTRA_ACTIVE_TAB, activeTab.index)

private const val KEY_PRESENTER_STATE = "presenter_state"

private const val EXTRA_USER_ID = "user_id"
private const val EXTRA_ACTIVE_TAB = "tab"
