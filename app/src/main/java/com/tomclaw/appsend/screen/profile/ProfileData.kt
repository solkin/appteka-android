package com.tomclaw.appsend.screen.profile

import android.os.Parcelable
import com.tomclaw.appsend.screen.profile.api.Profile
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProfileData(val profile: Profile, private val viewerId: Int) : Parcelable {
    val isSelf: Boolean
        get() = profile.userId == viewerId
}
