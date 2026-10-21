package com.tomclaw.appsend.screen.upload.adapter.origin

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class OriginItem(
    override val id: Long,
    val value: String?,
    val fixed: Boolean,
    val ownDenied: String?,
    val errorRequiredField: Boolean,
) : Item, Parcelable
