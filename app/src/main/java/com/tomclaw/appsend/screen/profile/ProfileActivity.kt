package com.tomclaw.appsend.screen.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.ThemeHelper

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val userId = intent.getIntExtra(EXTRA_USER_ID, 0)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_activity)

        if (savedInstanceState == null) {
            val fragment = createProfileFragment(userId)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.profile_fragment, fragment)
                .commit()
        }
    }

}

fun createProfileActivityIntent(
    context: Context,
    userId: Int,
): Intent = Intent(context, ProfileActivity::class.java)
    .putExtra(EXTRA_USER_ID, userId)

private const val EXTRA_USER_ID = "user_id"
