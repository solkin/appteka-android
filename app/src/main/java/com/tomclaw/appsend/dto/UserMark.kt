package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

/**
 * Lightweight "author tag" projection of a user — just enough to
 * render an avatar with a name and a primary badge overlay. It is
 * embedded inside content cards (chat messages, feed posts, app
 * info, app reviews, listings) wherever the user is a side
 * annotation rather than the subject of the row.
 *
 * Mirrors the backend `service/UserMark` Go struct: short JSON keys
 * (`id`/`name`/`icon`/`url`/`primary_badge`) because the wrapper
 * object is always nested under a parent field like `author` or
 * `user`, so the legacy `user_` prefix would be redundant.
 *
 * Carrying decorative state (badges, future subscription tiers) on
 * UserMark means every embedding picks new fields up for free —
 * adapters only depend on UserMark, not on per-DTO duplicate
 * fields.
 */
@Parcelize
@GsonModel
data class UserMark(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("icon")
    val icon: UserIcon? = null,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("primary_badge")
    val primaryBadge: BadgeMark? = null,
) : Parcelable
