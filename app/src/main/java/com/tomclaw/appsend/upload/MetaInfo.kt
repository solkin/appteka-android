package com.tomclaw.appsend.upload

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MetaInfo(
    val categoryId: Int,
    val description: String,
    val whatsNew: String,
    val exclusive: Boolean,
    val openSource: Boolean,
    val sourceUrl: String?
) : Parcelable
