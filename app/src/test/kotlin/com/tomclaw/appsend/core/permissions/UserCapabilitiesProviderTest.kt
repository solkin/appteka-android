package com.tomclaw.appsend.core.permissions

import io.reactivex.rxjava3.observers.TestObserver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The provider is the in-memory cache feeding every consumer of the
 * global capability snapshot. It's intentionally tiny — but the
 * BehaviorSubject semantics around late subscribers, nulls, and the
 * `clear()` reset are easy to get wrong, so we lean on TestObserver
 * to pin down the emitted sequence rather than just the final state.
 */
class UserCapabilitiesProviderTest {

    private val sampleA = mapOf(
        CapabilityAction.APP_UPLOAD to Capability(allowed = true),
        CapabilityAction.MODERATION_ENTER to Capability(allowed = false),
    )
    private val sampleB = mapOf(
        CapabilityAction.CHAT_TOPIC_CREATE to Capability(allowed = true),
    )

    @Test
    fun `getCapabilities is null until first set`() {
        val provider = UserCapabilitiesProviderImpl()
        assertNull(provider.getCapabilities())
    }

    @Test
    fun `set then get returns the same map instance`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(sampleA)
        assertSame(sampleA, provider.getCapabilities())
    }

    @Test
    fun `setCapabilities emits to current subscribers`() {
        val provider = UserCapabilitiesProviderImpl()
        val observer = TestObserver<Map<String, Capability>>()
        provider.observeCapabilities().subscribe(observer)

        provider.setCapabilities(sampleA)

        observer.assertValueCount(1)
        observer.assertValueAt(0, sampleA)
    }

    @Test
    fun `setCapabilities(null) keeps state null and does NOT emit`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(sampleA)

        val observer = TestObserver<Map<String, Capability>>()
        provider.observeCapabilities().subscribe(observer)
        observer.assertValueCount(1)

        provider.setCapabilities(null)

        assertNull(provider.getCapabilities())
        observer.assertValueCount(1)
    }

    @Test
    fun `clear emits an empty map and drops cached state`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(sampleA)
        val observer = TestObserver<Map<String, Capability>>()
        provider.observeCapabilities().subscribe(observer)
        observer.assertValueCount(1)

        provider.clear()

        assertNull(provider.getCapabilities())
        observer.assertValueCount(2)
        assertTrue(observer.values()[1].isEmpty())
    }

    @Test
    fun `late subscriber receives the latest snapshot`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(sampleA)
        provider.setCapabilities(sampleB)

        val observer = TestObserver<Map<String, Capability>>()
        provider.observeCapabilities().subscribe(observer)

        observer.assertValueCount(1)
        observer.assertValueAt(0, sampleB)
    }

    @Test
    fun `multiple subscribers each see the same emissions`() {
        val provider = UserCapabilitiesProviderImpl()
        val a = TestObserver<Map<String, Capability>>()
        val b = TestObserver<Map<String, Capability>>()

        provider.observeCapabilities().subscribe(a)
        provider.observeCapabilities().subscribe(b)

        provider.setCapabilities(sampleA)
        provider.setCapabilities(sampleB)

        a.assertValueCount(2)
        b.assertValueCount(2)
        assertEquals(sampleA, a.values()[0])
        assertEquals(sampleB, a.values()[1])
        assertEquals(sampleA, b.values()[0])
        assertEquals(sampleB, b.values()[1])
    }

    @Test
    fun `subscribe-after-clear receives the empty replay`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(sampleA)
        provider.clear()

        val observer = TestObserver<Map<String, Capability>>()
        provider.observeCapabilities().subscribe(observer)

        observer.assertValueCount(1)
        assertTrue(observer.values()[0].isEmpty())
    }

    @Test
    fun `clear before any set emits empty too`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.clear()
        assertNull(provider.getCapabilities())

        val observer = TestObserver<Map<String, Capability>>()
        provider.observeCapabilities().subscribe(observer)
        observer.assertValueCount(1)
        assertTrue(observer.values()[0].isEmpty())
    }

    @Test
    fun `setting an empty map is preserved as the current state`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(emptyMap())
        val cached = provider.getCapabilities()
        assertNotSame(null, cached)
        assertTrue(cached!!.isEmpty())
    }
}
