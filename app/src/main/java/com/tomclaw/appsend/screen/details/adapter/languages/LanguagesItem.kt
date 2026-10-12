package com.tomclaw.appsend.screen.details.adapter.languages

import android.os.Parcelable
import com.tomclaw.appsend.screen.details.api.AppLanguage
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

/**
 * Languages the app is translated into, in the order the server sent
 * them: the viewer's own language first when the app has it.
 */
@Parcelize
data class LanguagesItem(
    override val id: Long,
    val languages: List<AppLanguage>,
) : Item, Parcelable
