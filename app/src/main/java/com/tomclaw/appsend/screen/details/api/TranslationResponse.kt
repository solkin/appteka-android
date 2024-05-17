package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TranslationResponse(
    @SerializedName("whats_new")
    val whatsNew: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("whats_new_lang")
    val whatsNewLang: String?,
    @SerializedName("description_lang")
    val descriptionLang: String?,
) : Parcelable
