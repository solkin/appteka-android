package com.tomclaw.appsend.screen.upload.adapter.category

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.categories.CategoryItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectCategoryItem(
    override val id: Long,
    val category: CategoryItem?,
    val errorRequiredField: Boolean,
) : Item, Parcelable
