package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedAppItem(
    override val id: Long,
    val pkg: UploadPackage,
    val apk: UploadApk
) : Item, Parcelable
