package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
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
    @SerializedName("ai_note")
    val aiNote: String? = null,
    @SerializedName("ai_short_description")
    val aiShortDescription: String? = null,
) : Parcelable
