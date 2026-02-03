package com.tomclaw.appsend.core

import java.net.InetSocketAddress
import java.net.Proxy

data class ProxyConfig(
    val enabled: Boolean = false,
    val host: String = "",
    val port: Int = 0,
    val type: ProxyType = ProxyType.HTTP
) {

    fun toProxy(): Proxy? {
        if (!enabled || host.isBlank() || port <= 0) {
            return null
        }
        return Proxy(type.toJavaType(), InetSocketAddress(host, port))
    }

    fun isValid(): Boolean {
        return !enabled || (host.isNotBlank() && port > 0 && port <= 65535)
    }

}

enum class ProxyType {
    HTTP,
    SOCKS;

    fun toJavaType(): Proxy.Type = when (this) {
        HTTP -> Proxy.Type.HTTP
        SOCKS -> Proxy.Type.SOCKS
    }

    companion object {
        fun fromString(value: String): ProxyType {
            return when (value.uppercase()) {
                "SOCKS" -> SOCKS
                else -> HTTP
            }
        }
    }
}
