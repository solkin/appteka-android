package com.tomclaw.appsend.categories

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<Category>
) : Parcelable