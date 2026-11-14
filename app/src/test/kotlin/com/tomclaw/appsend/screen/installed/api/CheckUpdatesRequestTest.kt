package com.tomclaw.appsend.screen.installed.api

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tomclaw.appsend.dto.DeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckUpdatesRequestTest {

    // The server reads these names; renaming one silently turns the device
    // filter off.
    @Test
    fun `device goes out as abis in preference order and sdk`() {
        val request = CheckUpdatesRequest(
            locale = "en",
            apps = mapOf("com.example" to 7L),
            certs = mapOf("com.example" to "aaaa"),
            device = DeviceInfo(abis = listOf("arm64-v8a", "armeabi-v7a"), sdk = 34),
        )

        val json = JsonParser.parseString(Gson().toJson(request)).asJsonObject
        val device = json.getAsJsonObject("device")

        assertEquals(listOf("arm64-v8a", "armeabi-v7a"), device.getAsJsonArray("abis").map { it.asString })
        assertEquals(34, device.get("sdk").asInt)
        assertEquals("aaaa", json.getAsJsonObject("certs").get("com.example").asString)
    }

}
