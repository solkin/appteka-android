package com.tomclaw.appsend.util

import android.content.res.Resources
import androidx.core.app.NotificationCompat
import com.tomclaw.imageloader.core.ViewHolder
import com.tomclaw.imageloader.core.ViewSize

class NotificationIconHolder(
    resources: Resources,
    private val notificationBuilder: NotificationCompat.Builder
) : ViewHolder<NotificationCompat.Builder> {

    private val size = ViewSize(dpToPx(64, resources), dpToPx(64, resources))

    override var tag: Any?
        get() = notificationBuilder.extras.getString("tag")
        set(value) {
            notificationBuilder.extras.putString("tag", value.toString())
        }

    override fun get(): NotificationCompat.Builder {
        return notificationBuilder
    }

    override fun getSize(): ViewSize {
        return size
    }

    override fun optSize(): ViewSize {
        return size
    }
}
