package com.tomclaw.appsend.screen.post.adapter.submit

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubmitItem(
    override val id: Long,
    val enabled: Boolean,
) : Item, Parcelable
