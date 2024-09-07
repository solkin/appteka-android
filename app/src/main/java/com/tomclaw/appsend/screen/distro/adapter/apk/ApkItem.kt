package com.tomclaw.appsend.screen.distro.adapter.apk

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
class ApkItem(
    override val id: Long,
    val icon: String?,
    val title: String,
    val version: String,
    val size: Long,
    val createTime: Long,
    val packageName: String,
    val path: String,
    var isNew: Boolean,
) : Item, Parcelable
