package com.tomclaw.appsend.screen.details.adapter.screenshots

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Screenshot(
    val url: String,
    val width: Int,
    val height: Int,
) : Parcelable
