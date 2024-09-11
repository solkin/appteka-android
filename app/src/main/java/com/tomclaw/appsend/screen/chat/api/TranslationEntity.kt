package com.tomclaw.appsend.screen.chat.api

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TranslationEntity(
    val msgId: Int,
    val original: String,
    val translation: String,
    val lang: String,
    var translated: Boolean,
) : Parcelable
