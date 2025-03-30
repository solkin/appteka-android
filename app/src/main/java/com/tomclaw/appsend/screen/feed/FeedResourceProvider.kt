package com.tomclaw.appsend.screen.feed

import android.content.res.Resources
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.TimeProvider
import java.util.Locale

interface FeedResourceProvider {

    fun formatTime(value: Long): String

    fun prepareMenuActions(actions: List<String>, handler: (Int) -> Unit): List<MenuAction>

}

class FeedResourceProviderImpl(
    private val resources: Resources,
    private val locale: Locale,
    private val timeProvider: TimeProvider,
) : FeedResourceProvider {

    override fun formatTime(value: Long): String {
        return timeProvider.formatTimeDiff(value)
    }

    override fun prepareMenuActions(
        actions: List<String>,
        handler: (Int) -> Unit
    ): List<MenuAction> {
        return actions.mapIndexedNotNull { index, action ->
            when (action) {
                "delete" -> MenuAction(
                    id = index,
                    title = resources.getString(R.string.delete),
                    icon = R.drawable.ic_delete,
                    action = { handler.invoke(MENU_DELETE) }
                )

                else -> null
            }
        }
    }

}

const val MENU_DELETE = 1
