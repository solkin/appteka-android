package com.tomclaw.appsend.screen.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.home.createHomeActivityIntent
import com.tomclaw.appsend.util.updateTheme

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        var userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        var isShowHomeOnFinish = false

        val data = intent.data
        if (data != null && data.host != null) {
            if (data.host == "appteka.store" || data.host == "appteka.org") {
                val path = data.pathSegments
                if (path.size == 2) {
                    userId = path[1].toInt()
                    isShowHomeOnFinish = true
                }
            } else if (data.host == "appsend.store") {
                userId = data.getQueryParameter("id")?.toInt() ?: 0
                isShowHomeOnFinish = true
            }
        }

        if (userId == 0) {
            throw IllegalArgumentException("user ID must be provided")
        }

        updateTheme()

        super.onCreate(savedInstanceState)
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
