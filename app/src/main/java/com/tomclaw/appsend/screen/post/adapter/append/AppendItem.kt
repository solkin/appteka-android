package com.tomclaw.appsend.screen.post.adapter.append

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppendItem(
    override val id: Long,
) : Item, Parcelable
