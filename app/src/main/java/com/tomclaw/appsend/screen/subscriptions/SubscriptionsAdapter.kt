package com.tomclaw.appsend.screen.subscriptions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.profile.createProfileFragment
import com.tomclaw.appsend.screen.users.UsersType
import com.tomclaw.appsend.screen.users.createUsersFragment

class SubscriptionsAdapter internal constructor(
    fm: FragmentManager,
    lifecycle: Lifecycle,
    val userId: Int,
) : FragmentStateAdapter(fm, lifecycle) {

    fun getItemTitle(position: Int): Int {
        return when (position) {
            0 -> R.string.subscribers
            1 -> R.string.publishers
            else -> throw Exception("Invalid item index")
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> createUsersFragment(userId, UsersType.SUBSCRIBERS)
            1 -> createUsersFragment(userId, UsersType.PUBLISHERS)
            else -> throw IllegalStateException("Invalid fragment index")
        }
        return fragment
    }
}