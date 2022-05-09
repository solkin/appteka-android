package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Scores(
    @SerializedName("five")
    val five: Int,
    @SerializedName("four")
    val four: Int,
    @SerializedName("three")
    val three: Int,
    @SerializedName("two")
    val two: Int,
    @SerializedName("one")
    val one: Int,
) : Parcelable
