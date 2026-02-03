package com.tomclaw.appsend.core

import java.io.IOException
import java.net.Proxy
import java.net.ProxySelector
import java.net.SocketAddress
import java.net.URI

class DynamicProxySelector(
    private val proxyConfigProvider: ProxyConfigProvider
) : ProxySelector() {

    override fun select(uri: URI?): List<Proxy> {
        val proxy = proxyConfigProvider.getProxyConfig().toProxy()
        return if (proxy != null) {
            listOf(proxy)
        } else {
            listOf(Proxy.NO_PROXY)
        }
    }

    override fun connectFailed(uri: URI?, sa: SocketAddress?, ioe: IOException?) {
        // No-op: we don't track connection failures
    }

}
