package com.tomclaw.appsend.screen.subscriptions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.subscribers.createSubscribersFragment
import com.tomclaw.appsend.util.ThemeHelper

class SubscriptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        val activeTab = intent.getIntExtra(EXTRA_ACTIVE_TAB, Tab.SUBSCRIBERS.index)

        if (userId == 0) {
            throw IllegalArgumentException("user ID must be provided")
        }

        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.subscriptions_activity)

        if (savedInstanceState == null) {
            val fragment = createSubscribersFragment(userId)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit()
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                leaveScreen()
            }
        })
    }

    private fun leaveScreen() {
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

private const val EXTRA_USER_ID = "user_id"
private const val EXTRA_ACTIVE_TAB = "tab"
