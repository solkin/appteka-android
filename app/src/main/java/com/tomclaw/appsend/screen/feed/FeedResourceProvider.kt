package com.tomclaw.appsend.screen.feed

import android.content.res.Resources
import com.tomclaw.appsend.core.TimeProvider
import java.util.Locale

interface FeedResourceProvider {

    fun formatTime(value: Long): String

}

class FeedResourceProviderImpl(
    private val resources: Resources,
    private val locale: Locale,
    private val timeProvider: TimeProvider,
) : FeedResourceProvider {

    override fun formatTime(value: Long): String {
        return timeProvider.formatTimeDiff(value)
    }

}