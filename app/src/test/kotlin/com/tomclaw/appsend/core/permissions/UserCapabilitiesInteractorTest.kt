package com.tomclaw.appsend.core.permissions

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.permissions.api.UserCapabilitiesResponse
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * Verifies the contract between [UserCapabilitiesInteractor] and the
 * provider it caches into. The interactor itself is thin —
 * subscribeOn(io), `doOnSuccess { provider.set... }` — but its job is
 * load-bearing for every "global FAB" decision, so we cover both the
 * success and failure paths and the off-thread scheduling.
 *
 * StoreApi is a 50+-method interface; rather than stub each method we
 * use a JDK dynamic proxy that only services `getUserCapabilities()`
 * and shouts on anything else. This keeps the test self-contained
 * with no Mockito dependency.
 */
class UserCapabilitiesInteractorTest {

    private val response = UserCapabilitiesResponse(
        role = 0,
        accessList = listOf(3000),
        capabilities = mapOf(
            CapabilityAction.APP_UPLOAD to Capability(allowed = true),
            CapabilityAction.MODERATION_ENTER to Capability(allowed = false),
        ),
    )

    @Test
    fun `refresh resolves with the response and updates the provider`() {
        val provider = UserCapabilitiesProviderImpl()
        val api = stubApi(Single.just(StoreResponse(200, response, "")))

        val interactor = UserCapabilitiesInteractorImpl(api, provider, immediateSchedulers())
        val out = interactor.refresh().blockingGet()

        assertEquals(response, out)
        assertEquals(response.capabilities, provider.getCapabilities())
    }

    @Test
    fun `refresh propagates errors and does NOT touch the provider`() {
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(
            mapOf(CapabilityAction.APP_UPLOAD to Capability(allowed = true)),
        )
        val before = provider.getCapabilities()

        val api = stubApi(Single.error(RuntimeException("network down")))
        val interactor = UserCapabilitiesInteractorImpl(api, provider, immediateSchedulers())

        var caught: Throwable? = null
        try {
            interactor.refresh().blockingGet()
        } catch (e: RuntimeException) {
            caught = e
        }
        assertNotNull("interactor must propagate the upstream error", caught)
        // Cache is unchanged: the previous "good" snapshot survives a
        // failed refresh, so transient network blips don't blank out
        // the FAB visibility on every poll.
        assertSame(before, provider.getCapabilities())
    }

    @Test
    fun `refresh with null capabilities still propagates and clears the provider snapshot`() {
        // The DTO marks capabilities as nullable: server may legitimately
        // omit it. provider.set(null) does NOT emit (by design — see
        // UserCapabilitiesProviderTest), so the snapshot drops to null
        // silently and consumers fall back to Unknown.
        val provider = UserCapabilitiesProviderImpl()
        provider.setCapabilities(
            mapOf(CapabilityAction.APP_UPLOAD to Capability(allowed = true)),
        )

        val emptyResponse = UserCapabilitiesResponse(role = 0, accessList = null, capabilities = null)
        val api = stubApi(Single.just(StoreResponse(200, emptyResponse, "")))
        val interactor = UserCapabilitiesInteractorImpl(api, provider, immediateSchedulers())

        interactor.refresh().blockingGet()

        assertNull(provider.getCapabilities())
    }

    @Test
    fun `refresh runs on the io scheduler`() {
        // Trivial smoke test that we honour the schedulers contract.
        // Use a counter scheduler that records subscription threads —
        // simpler than spying on Schedulers.io().
        var subscribedOnCustomIo = false
        val customIo = io.reactivex.rxjava3.schedulers.Schedulers.from { runnable ->
            subscribedOnCustomIo = true
            runnable.run()
        }
        val schedulers = object : SchedulersFactory {
            override fun io(): Scheduler = customIo
            override fun mainThread(): Scheduler = Schedulers.trampoline()
        }
        val api = stubApi(Single.just(StoreResponse(200, response, "")))
        val provider = UserCapabilitiesProviderImpl()

        UserCapabilitiesInteractorImpl(api, provider, schedulers)
            .refresh()
            .blockingGet()

        assertEquals(true, subscribedOnCustomIo)
    }

    // --- helpers ----------------------------------------------------

    /**
     * Build a [StoreApi] stub that only services `getUserCapabilities()`.
     * Any other call from the SUT means we wired the wrong method —
     * fail loudly.
     */
    private fun stubApi(
        getUserCapabilities: Single<StoreResponse<UserCapabilitiesResponse>>,
    ): StoreApi {
        val handler = InvocationHandler { _, method: Method, _ ->
            check(method.name == "getUserCapabilities") {
                "unexpected StoreApi call from interactor: ${method.name}"
            }
            getUserCapabilities
        }
        return Proxy.newProxyInstance(
            StoreApi::class.java.classLoader,
            arrayOf(StoreApi::class.java),
            handler,
        ) as StoreApi
    }

    private fun immediateSchedulers(): SchedulersFactory = object : SchedulersFactory {
        override fun io(): Scheduler = Schedulers.trampoline()
        override fun mainThread(): Scheduler = Schedulers.trampoline()
    }
}
