package com.tomclaw.appsend.di.legacy

import com.tomclaw.appsend.core.StoreApi
import okhttp3.OkHttpClient
import javax.inject.Inject

class LegacyInjector {

    @Inject
    lateinit var api: StoreApi

    @Inject
    lateinit var httpClient: OkHttpClient

}