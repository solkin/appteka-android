package com.tomclaw.appsend.di.legacy

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.Analytics
import okhttp3.OkHttpClient
import javax.inject.Inject

class LegacyInjector {

    @Inject
    lateinit var api: StoreApi

    @Inject
    lateinit var httpClient: OkHttpClient

    @Inject
    lateinit var analytics: Analytics

}
