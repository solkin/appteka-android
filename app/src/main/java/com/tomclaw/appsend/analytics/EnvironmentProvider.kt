package com.tomclaw.appsend.analytics

import android.os.Build
import com.tomclaw.appsend.analytics.api.Environment
import com.tomclaw.appsend.core.AppInfoProvider
import com.tomclaw.appsend.core.DeviceIdProvider
import java.util.Locale

interface EnvironmentProvider {

    fun environment(): Environment

}

class EnvironmentProviderImpl(
    private val locale: Locale,
    private val infoProvider: AppInfoProvider,
    private val idProvider: DeviceIdProvider,
) : EnvironmentProvider {

    override fun environment() = Environment(
        packageName = infoProvider.getPackageName(),
        appVersion = infoProvider.getVersionCode(),
        deviceId = idProvider.getDeviceId(),
        osVersion = Build.VERSION.SDK_INT,
        manufacturer = Build.MANUFACTURER,
        model = Build.MODEL,
        country = locale.country,
        language = locale.language,
    )

}
