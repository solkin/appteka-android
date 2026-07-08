package com.tomclaw.appsend.screen.store

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// A selectable category entry for the Store filter (the chip label and
// the category picker bottom sheet). id 0 is the "All categories" entry.
@Parcelize
data class CategoryDropdownItem(
    val id: Int,
    val title: String,
    val iconSvg: String?,
    val iconRes: Int
) : Parcelable
