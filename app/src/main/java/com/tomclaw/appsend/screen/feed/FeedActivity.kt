package com.tomclaw.appsend.screen.feed

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R

class FeedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0)

        if (userId == 0) {
            throw IllegalArgumentException("user ID must be provided")
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.feed_activity)

        if (savedInstanceState == null) {
            val fragment = createFeedFragment(userId, withToolbar = true)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.feed_fragment, fragment)
                .commit()
        }
    }

}

fun createFeedActivityIntent(
    context: Context,
    userId: Int,
): Intent = Intent(context, FeedActivity::class.java)
    .putExtra(EXTRA_USER_ID, userId)

private const val EXTRA_USER_ID = "user_id"
