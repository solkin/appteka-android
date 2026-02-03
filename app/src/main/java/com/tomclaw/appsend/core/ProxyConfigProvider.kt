package com.tomclaw.appsend.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

interface ProxyConfigProvider {

    fun getProxyConfig(): ProxyConfig

    fun setProxyConfig(config: ProxyConfig)

    fun observeProxyConfig(): Observable<ProxyConfig>

}

class ProxyConfigProviderImpl(context: Context) : ProxyConfigProvider {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val proxyConfigSubject: BehaviorSubject<ProxyConfig> = BehaviorSubject.createDefault(getProxyConfig())

    override fun getProxyConfig(): ProxyConfig {
        return ProxyConfig(
            enabled = preferences.getBoolean(PREF_PROXY_ENABLED, false),
            host = preferences.getString(PREF_PROXY_HOST, "") ?: "",
            port = preferences.getInt(PREF_PROXY_PORT, 0),
            type = ProxyType.fromString(preferences.getString(PREF_PROXY_TYPE, "HTTP") ?: "HTTP")
        )
    }

    override fun setProxyConfig(config: ProxyConfig) {
        preferences.edit()
            .putBoolean(PREF_PROXY_ENABLED, config.enabled)
            .putString(PREF_PROXY_HOST, config.host)
            .putInt(PREF_PROXY_PORT, config.port)
            .putString(PREF_PROXY_TYPE, config.type.name)
            .apply()
        proxyConfigSubject.onNext(config)
    }

    override fun observeProxyConfig(): Observable<ProxyConfig> {
        return proxyConfigSubject.distinctUntilChanged()
    }

    companion object {
        const val PREF_PROXY_ENABLED = "pref_proxy_enabled"
        const val PREF_PROXY_HOST = "pref_proxy_host"
        const val PREF_PROXY_PORT = "pref_proxy_port"
        const val PREF_PROXY_TYPE = "pref_proxy_type"
    }

}
