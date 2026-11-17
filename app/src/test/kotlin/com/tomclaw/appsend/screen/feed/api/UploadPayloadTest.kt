package com.tomclaw.appsend.screen.feed.api

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UploadPayloadTest {

    // The server names the announced release "ref" and leaves it out for a
    // build without a signing key; such posts open by app_id.
    @Test
    fun `release address is read when present and absent otherwise`() {
        val keyed = Gson().fromJson(payload(""""ref":"com.foo/a1b2c3d4e5f6a7b8c9d0e1f2/12","""), UploadPayload::class.java)
        val unkeyed = Gson().fromJson(payload(""), UploadPayload::class.java)

        assertEquals("com.foo/a1b2c3d4e5f6a7b8c9d0e1f2/12", keyed.ref)
        assertEquals("abcr1", keyed.appId)
        assertNull(unkeyed.ref)
    }

    private fun payload(ref: String) = """{"app_id":"abcr1",$ref"package":"com.foo","label":"Foo",
        "ver_name":"1.2","ver_code":12,"size":1,"file_status":0,"exclusive":false,"open_source":false}"""

}
