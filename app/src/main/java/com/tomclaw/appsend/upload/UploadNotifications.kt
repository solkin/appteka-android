package com.tomclaw.appsend.upload

import android.app.Notification
import android.content.Context
import com.tomclaw.appsend.main.item.CommonItem
import io.reactivex.rxjava3.core.Observable

interface UploadNotifications {

    fun subscribe(
        id: String,
        item: CommonItem,
        start: (Int, Notification) -> Unit,
        stop: () -> Unit,
        observable: Observable<Int>
    )

}

class UploadNotificationsImpl(context: Context) : UploadNotifications {

    override fun subscribe(
        id: String,
        item: CommonItem,
        start: (Int, Notification) -> Unit,
        stop: () -> Unit,
        observable: Observable<Int>
    ) {

    }

}
