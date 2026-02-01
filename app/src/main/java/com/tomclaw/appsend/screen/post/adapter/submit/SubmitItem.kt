package com.tomclaw.appsend.screen.post.adapter.submit

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubmitItem(
    override val id: Long,
) : Item, Parcelable
