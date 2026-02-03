package com.tomclaw.appsend.dto

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class StoreResponse<A>(
    @SerializedName("status")
    val status: Int,
    @SerializedName("result")
    val result: A,
    @SerializedName("description")
    val description: String
)
