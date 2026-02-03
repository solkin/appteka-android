package com.tomclaw.appsend.core

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

sealed class ProxyCheckResult {
    object Success : ProxyCheckResult()
    data class Error(val message: String) : ProxyCheckResult()
}

interface ProxyChecker {
    fun check(config: ProxyConfig): Single<ProxyCheckResult>
}

class ProxyCheckerImpl : ProxyChecker {

    override fun check(config: ProxyConfig): Single<ProxyCheckResult> {
        return Single.fromCallable {
            val proxy = createProxy(config)
            val client = createClient(proxy)
            val request = Request.Builder()
                .url(CHECK_URL)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    ProxyCheckResult.Success
                } else {
                    ProxyCheckResult.Error("HTTP ${response.code}")
                }
            }
        }
            .subscribeOn(Schedulers.io())
            .onErrorReturn { throwable ->
                ProxyCheckResult.Error(formatError(throwable))
            }
    }

    private fun createProxy(config: ProxyConfig): Proxy {
        return Proxy(
            config.type.toJavaType(),
            InetSocketAddress(config.host, config.port)
        )
    }

    private fun createClient(proxy: Proxy): OkHttpClient {
        return OkHttpClient.Builder()
            .proxy(proxy)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private fun formatError(throwable: Throwable): String {
        return when {
            throwable.message?.contains("timeout", ignoreCase = true) == true -> "Timeout"
            throwable.message?.contains("refused", ignoreCase = true) == true -> "Connection refused"
            throwable.message?.contains("unreachable", ignoreCase = true) == true -> "Host unreachable"
            throwable.message?.contains("unknown host", ignoreCase = true) == true -> "Unknown host"
            throwable.message?.contains("no route", ignoreCase = true) == true -> "No route to host"
            else -> throwable.message ?: throwable.javaClass.simpleName
        }
    }

    companion object {
        private const val CHECK_URL = "https://appteka.store/api/1/app/list"
        private const val TIMEOUT_SECONDS = 10L
    }
}
