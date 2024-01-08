package com.tomclaw.appsend.screen.details.adapter.screenshots

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Screenshot(
    val uri: Uri,
    val width: Int,
    val height: Int,
) : Parcelable
