package com.tomclaw.appsend.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Expected values follow the server's keyOf in common/apksign.
class EditionKeyTest {

    @Test
    fun `no signers give no key`() {
        assertNull(editionKeyOf(emptyList()))
    }

    @Test
    fun `one signer is the hash of its certificate`() {
        assertEquals(
            "f45ad729da07698c45b72dae8273a9e105585830214e978d6614f56ac93ca1b6",
            editionKeyOf(listOf("cert-a".toByteArray()))
        )
    }

    @Test
    fun `several signers hash their sorted hashes in any order`() {
        val expected = "8a3f0460a2a2be22b4c315c238673aba91da9709bd56807713a8c3f3029f1a78"
        val a = "cert-a".toByteArray()
        val b = "cert-b".toByteArray()

        assertEquals(expected, editionKeyOf(listOf(a, b)))
        assertEquals(expected, editionKeyOf(listOf(b, a)))
    }

}
