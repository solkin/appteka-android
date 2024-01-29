package com.tomclaw.appsend.core

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import android.provider.Settings.Secure.ANDROID_ID

interface DeviceIdProvider {

    fun getDeviceId(): String

}

class DeviceIdProviderImpl(
    private val context: Context
) : DeviceIdProvider {

    @SuppressLint("HardwareIds")
    override fun getDeviceId(): String {
        return Settings.Secure.getString(context.contentResolver, ANDROID_ID)
    }

}
