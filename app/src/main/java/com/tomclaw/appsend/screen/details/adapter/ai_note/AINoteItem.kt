package com.tomclaw.appsend.screen.details.adapter.ai_note

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class AINoteItem(
    override val id: Long,
    val appId: String,
    val state: AINoteState,
    val note: String?,
) : Item, Parcelable

enum class AINoteState {
    IDLE,
    PENDING,
    COMPLETED,
}
