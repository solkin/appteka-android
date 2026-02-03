package com.tomclaw.appsend.core

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ProxyAddressParserTest(
    private val input: String,
    private val expected: ParsedProxyAddress?
) {

    @Test
    fun parseProxyAddressTest() {
        val result = ProxyAddressParser.parse(input)
        assertEquals(expected, result)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "input=\"{0}\" -> {1}")
        fun data(): Collection<Array<Any?>> {
            return listOf(
                // HTTP scheme
                arrayOf(
                    "http://proxy.example.com:8080",
                    ParsedProxyAddress("proxy.example.com", 8080, ProxyType.HTTP)
                ),
                arrayOf(
                    "https://secure.proxy.com:443",
                    ParsedProxyAddress("secure.proxy.com", 443, ProxyType.HTTP)
                ),
                arrayOf(
                    "HTTP://UPPERCASE.COM:3128",
                    ParsedProxyAddress("UPPERCASE.COM", 3128, ProxyType.HTTP)
                ),

                // SOCKS schemes
                arrayOf(
                    "socks://socks.proxy.com:1080",
                    ParsedProxyAddress("socks.proxy.com", 1080, ProxyType.SOCKS)
                ),
                arrayOf(
                    "socks4://socks4.proxy.com:1080",
                    ParsedProxyAddress("socks4.proxy.com", 1080, ProxyType.SOCKS)
                ),
                arrayOf(
                    "socks5://socks5.proxy.com:1080",
                    ParsedProxyAddress("socks5.proxy.com", 1080, ProxyType.SOCKS)
                ),
                arrayOf(
                    "socks4a://socks4a.proxy.com:1080",
                    ParsedProxyAddress("socks4a.proxy.com", 1080, ProxyType.SOCKS)
                ),
                arrayOf(
                    "socks5h://socks5h.proxy.com:1080",
                    ParsedProxyAddress("socks5h.proxy.com", 1080, ProxyType.SOCKS)
                ),
                arrayOf(
                    "SOCKS5://uppercase.socks.com:9050",
                    ParsedProxyAddress("uppercase.socks.com", 9050, ProxyType.SOCKS)
                ),

                // Host:port without scheme (type should be null)
                arrayOf(
                    "proxy.example.com:8080",
                    ParsedProxyAddress("proxy.example.com", 8080, null)
                ),
                arrayOf(
                    "localhost:3128",
                    ParsedProxyAddress("localhost", 3128, null)
                ),
                arrayOf(
                    "192.168.1.1:8888",
                    ParsedProxyAddress("192.168.1.1", 8888, null)
                ),

                // IPv6 with port
                arrayOf(
                    "[::1]:8080",
                    ParsedProxyAddress("::1", 8080, null)
                ),
                arrayOf(
                    "[2001:db8::1]:3128",
                    ParsedProxyAddress("2001:db8::1", 3128, null)
                ),
                arrayOf(
                    "[fe80::1%eth0]:1080",
                    ParsedProxyAddress("fe80::1%eth0", 1080, null)
                ),

                // IPv6 with scheme
                arrayOf(
                    "http://[::1]:8080",
                    ParsedProxyAddress("::1", 8080, ProxyType.HTTP)
                ),
                arrayOf(
                    "socks5://[2001:db8::1]:1080",
                    ParsedProxyAddress("2001:db8::1", 1080, ProxyType.SOCKS)
                ),
                arrayOf(
                    "https://[fe80::1]:443",
                    ParsedProxyAddress("fe80::1", 443, ProxyType.HTTP)
                ),

                // Scheme with host but no port
                arrayOf(
                    "http://proxy.example.com",
                    ParsedProxyAddress("proxy.example.com", null, ProxyType.HTTP)
                ),
                arrayOf(
                    "socks5://socks.proxy.com",
                    ParsedProxyAddress("socks.proxy.com", null, ProxyType.SOCKS)
                ),
                arrayOf(
                    "http://[::1]",
                    ParsedProxyAddress("::1", null, ProxyType.HTTP)
                ),

                // Whitespace handling
                arrayOf(
                    "  http://proxy.com:8080  ",
                    ParsedProxyAddress("proxy.com", 8080, ProxyType.HTTP)
                ),
                arrayOf(
                    "\tproxy.com:3128\n",
                    ParsedProxyAddress("proxy.com", 3128, null)
                ),

                // Invalid inputs (should return null)
                arrayOf<Any?>(
                    "",
                    null
                ),
                arrayOf<Any?>(
                    "   ",
                    null
                ),
                arrayOf<Any?>(
                    "just-a-host",
                    null
                ),
                arrayOf<Any?>(
                    "proxy.com",
                    null
                ),

                // Invalid port numbers
                arrayOf<Any?>(
                    "proxy.com:0",
                    null
                ),
                arrayOf<Any?>(
                    "proxy.com:65536",
                    null
                ),
                arrayOf<Any?>(
                    "proxy.com:-1",
                    null
                ),
                arrayOf<Any?>(
                    "proxy.com:abc",
                    null
                ),
            )
        }
    }
}
