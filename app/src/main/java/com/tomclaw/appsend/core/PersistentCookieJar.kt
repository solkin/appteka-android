package com.tomclaw.appsend.core

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class PersistentCookieJar(filesDir: File) : CookieJar {

    private val db = File(filesDir, "cookies.dat")
    private var loaded = false
    private var cache: MutableList<Cookie> = mutableListOf()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cache.clear()
        cache.addAll(cookies)

        val persistentCookies = cookies.filter { it.persistent }
        DataOutputStream(FileOutputStream(db)).use { output ->
            output.writeShort(persistentCookies.size)
            persistentCookies.forEach { cookie ->
                output.writeUTF(cookie.name)
                output.writeUTF(cookie.value)
                output.writeLong(cookie.expiresAt)
                output.writeUTF(cookie.domain)
                output.writeUTF(cookie.path)
                output.writeBoolean(cookie.secure)
                output.writeBoolean(cookie.httpOnly)
                output.writeBoolean(cookie.hostOnly)
            }
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        if (!loaded) {
            val cookies = mutableListOf<Cookie>()
            try {
                DataInputStream(FileInputStream(db)).use { input ->
                    val size = input.readShort()
                    for (i in 0 until size) {
                        val name = input.readUTF()
                        val value = input.readUTF()
                        val expiresAt = input.readLong()
                        val domain = input.readUTF()
                        val path = input.readUTF()
                        val secure = input.readBoolean()
                        val httpOnly = input.readBoolean()
                        val hostOnly = input.readBoolean()
                        val builder = Cookie.Builder()
                            .name(name)
                            .value(value)
                            .expiresAt(expiresAt)
                            .path(path)
                        if (secure) {
                            builder.secure()
                        }
                        if (httpOnly) {
                            builder.httpOnly()
                        }
                        if (hostOnly) {
                            builder.hostOnlyDomain(domain)
                        } else {
                            builder.domain(domain)
                        }
                        cookies.add(builder.build())
                    }
                }
            } catch (ex: Throwable) {
                println("[CookieJar] Error while loading storage: $ex")
            }
            loaded = true
            cache.clear()
            cache.addAll(cookies)
        }

        return cache.toList()
    }

}
