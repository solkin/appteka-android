package com.tomclaw.appsend.screen.upload.adapter.selected_app

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.upload.UploadApk
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedAppItem(
    override val id: Long,
    val apk: UploadApk
) : Item, Parcelable
