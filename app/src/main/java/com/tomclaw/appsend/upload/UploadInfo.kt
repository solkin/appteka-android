package com.tomclaw.appsend.upload

import android.os.Parcelable
import com.tomclaw.appsend.categories.CategoryItem
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadInfo(
    val checkExist: CheckExistResponse,
    val category: CategoryItem,
    val description: String,
    val whatsNew: String,
    val exclusive: Boolean,
    val openSource: Boolean,
    val sourceUrl: String?
) : Parcelable
