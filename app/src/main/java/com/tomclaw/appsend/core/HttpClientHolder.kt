package com.tomclaw.appsend.core

import android.app.Application
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ConnectionPool
import okhttp3.CookieJar
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface HttpClientHolder {

    fun getClient(): OkHttpClient

}

class HttpClientHolderImpl(
    app: Application,
    cookieJar: CookieJar,
    userAgentProvider: UserAgentProvider,
    deviceIdProvider: DeviceIdProvider,
    appInfoProvider: AppInfoProvider,
    proxyConfigProvider: ProxyConfigProvider
) : HttpClientHolder {

    private val connectionPool = ConnectionPool()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectionPool(connectionPool)
        .readTimeout(2, TimeUnit.MINUTES)
        .connectTimeout(20, TimeUnit.SECONDS)
        .proxySelector(DynamicProxySelector(proxyConfigProvider))
        .addInterceptor(UserAgentInterceptor(userAgentProvider.getUserAgent()))
        .addInterceptor(DeviceIdInterceptor(deviceIdProvider.getDeviceId()))
        .addInterceptor(AppInfoInterceptor(appInfoProvider))
        .addInterceptor(ChuckerInterceptor.Builder(app).build())
        .cookieJar(cookieJar)
        .build()

    @Suppress("unused")
    private val disposable: Disposable = proxyConfigProvider.observeProxyConfig()
        .skip(1) // Skip initial value
        .observeOn(Schedulers.io())
        .subscribe(
            { connectionPool.evictAll() },
            { /* Ignore errors */ }
        )

    override fun getClient(): OkHttpClient = client

}
