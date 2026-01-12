package com.tomclaw.appsend.screen.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.screen.home.createHomeActivityIntent
import com.tomclaw.appsend.util.updateTheme

class ProfileActivity : AppCompatActivity() {

    private val deepLinkParser: ProfileDeepLinkParser by lazy {
        appComponent.profileDeepLinkParser()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var userId: Int
        var isShowHomeOnFinish = false

        val data = intent.data
        if (data != null) {
            when (val deepLink = deepLinkParser.parse(data)) {
                is ProfileDeepLink.ByUserId -> {
                    userId = deepLink.userId
                    isShowHomeOnFinish = true
                }
                is ProfileDeepLink.Invalid -> return navigateToStore()
            }
        } else {
            userId = intent.getIntExtra(EXTRA_USER_ID, 0)
            if (userId == 0) {
                return navigateToStore()
            }
        }

        updateTheme()
        setContentView(R.layout.profile_activity)

        if (savedInstanceState == null) {
            val fragment = createProfileFragment(userId)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.profile_fragment, fragment)
                .commit()
        }

        // Only intercept back if we need to show home screen (deep link case)
        if (isShowHomeOnFinish) {
            onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    leaveScreen()
                }
            })
        }
    }

    private fun leaveScreen() {
        navigateToStore()
    }

    private fun navigateToStore() {
        val intent = createHomeActivityIntent(context = this).apply {
            setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent)
        finish()
    }

}

fun createProfileActivityIntent(
    context: Context,
    userId: Int,
): Intent = Intent(context, ProfileActivity::class.java)
    .putExtra(EXTRA_USER_ID, userId)

private const val EXTRA_USER_ID = "user_id"
