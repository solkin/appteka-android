package com.tomclaw.appsend.screen.installed.api

import com.google.gson.Gson
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CheckUpdatesRequestTest {

    // The server reads these names; the device travels in headers instead.
    @Test
    fun `request carries installed versions and keys, not the device`() {
        val request = CheckUpdatesRequest(
            locale = "en",
            apps = mapOf("com.example" to 7L),
            certs = mapOf("com.example" to "aaaa"),
        )

        val json = JsonParser.parseString(Gson().toJson(request)).asJsonObject

        assertEquals("en", json.get("locale").asString)
        assertEquals(7L, json.getAsJsonObject("apps").get("com.example").asLong)
        assertEquals("aaaa", json.getAsJsonObject("certs").get("com.example").asString)
        assertFalse(json.has("device"))
    }

}
