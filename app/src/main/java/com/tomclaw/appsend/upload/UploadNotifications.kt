package com.tomclaw.appsend.upload

import android.app.Notification
import android.content.Context
import io.reactivex.rxjava3.core.Observable

interface UploadNotifications {

    fun subscribe(
        id: String,
        file: String,
        start: (Int, Notification) -> Unit,
        stop: () -> Unit,
        observable: Observable<Int>
    )

}

class UploadNotificationsImpl(context: Context) : UploadNotifications {

    override fun subscribe(
        id: String,
        file: String,
        start: (Int, Notification) -> Unit,
        stop: () -> Unit,
        observable: Observable<Int>
    ) {
        TODO("Not yet implemented")
    }

}
