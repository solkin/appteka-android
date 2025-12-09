package com.tomclaw.appsend.screen.subscriptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.subscriptions.di.SubscriptionsModule
// import com.tomclaw.appsend.util.updateTheme
import javax.inject.Inject

class SubscriptionsActivity : AppCompatActivity(), SubscriptionsPresenter.SubscriptionsRouter {

    @Inject
    lateinit var presenter: SubscriptionsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0).takeIf { it != 0 }
            ?: throw IllegalArgumentException("user ID must be provided")
        val name = intent.getStringExtra(EXTRA_ACTIVE_TAB) ?: Tab.SUBSCRIBERS.name
        val activeTab = Tab.valueOf(name)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .subscriptionsComponent(SubscriptionsModule(userId, presenterState))
            .inject(activity = this)
        // updateTheme()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.subscriptions_activity)

        val adapter = SubscriptionsAdapter(supportFragmentManager, lifecycle, userId)
        val view = SubscriptionsViewImpl(window.decorView, adapter)
        view.setSelectedPage(activeTab.ordinal)

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

    override fun leaveScreen() {
        finish()
    }

}

enum class Tab {
    SUBSCRIBERS,
    PUBLISHERS,
}

fun createSubscriptionsActivityIntent(
    context: Context,
    userId: Int,
    activeTab: Tab,
): Intent = Intent(context, SubscriptionsActivity::class.java)
    .putExtra(EXTRA_USER_ID, userId)
    .putExtra(EXTRA_ACTIVE_TAB, activeTab.name)

private const val KEY_PRESENTER_STATE = "presenter_state"

private const val EXTRA_USER_ID = "user_id"
private const val EXTRA_ACTIVE_TAB = "tab"
