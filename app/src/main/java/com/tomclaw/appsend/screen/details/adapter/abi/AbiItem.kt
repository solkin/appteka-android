package com.tomclaw.appsend.screen.details.adapter.abi

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class AbiItem(
    override val id: Long,
    val abiList: List<String>,
    val isCompatible: Boolean,
) : Item, Parcelable
