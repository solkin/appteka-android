package com.tomclaw.appsend.screen.post.adapter.text

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class TextItem(
    override val id: Long,
    val text: String,
    val errorRequiredField: Boolean,
    val maxLength: Int,
) : Item, Parcelable
