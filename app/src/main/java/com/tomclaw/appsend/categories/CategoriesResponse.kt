package com.tomclaw.appsend.categories

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<Category>
) : Parcelable
