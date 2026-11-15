package com.tomclaw.appsend.screen.auth.verify_code

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.UserCapabilitiesInteractorImpl
import com.tomclaw.appsend.core.permissions.UserCapabilitiesProviderImpl
import com.tomclaw.appsend.core.permissions.api.UserCapabilitiesResponse
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.auth.verify_code.api.VerifyCodeResponse
import com.tomclaw.appsend.user.api.UserProfile
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.SingleSubject
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

class VerifyCodeInteractorTest {
    private val schedulers = object : SchedulersFactory {
        override fun io(): Scheduler = Schedulers.trampoline()
        override fun mainThread(): Scheduler = Schedulers.trampoline()
    }
    private val login = VerifyCodeResponse(UserProfile(7, UserIcon("", color = ""), 0, 0, 0, "QA", true, null))
    private val guest = mapOf(CapabilityAction.BIO_EDIT to Capability(false))
    private val allowed = mapOf(CapabilityAction.BIO_EDIT to Capability(true))

    @Test
    fun `successful login replaces guest capabilities before completing`() {
        val pending = SingleSubject.create<StoreResponse<UserCapabilitiesResponse>>()
        val provider = UserCapabilitiesProviderImpl().apply { setCapabilities(guest) }
        val api = api(Single.just(StoreResponse(200, login, "")), pending)
        val capabilities = UserCapabilitiesInteractorImpl(api, provider, schedulers)
        val observer = VerifyCodeInteractorImpl(api, capabilities, schedulers).verifyCode("request", "123456", null).test()
        assertNull(provider.getCapabilities())
        observer.assertNoValues()
        pending.onSuccess(StoreResponse(200, UserCapabilitiesResponse(0, null, allowed), ""))
        observer.assertValue(login).assertComplete()
        assertEquals(allowed, provider.getCapabilities())
    }

    @Test
    fun `failed capabilities leave login valid with unknown rights`() {
        val pending = SingleSubject.create<StoreResponse<UserCapabilitiesResponse>>()
        val provider = UserCapabilitiesProviderImpl().apply { setCapabilities(guest) }
        val api = api(Single.just(StoreResponse(200, login, "")), pending)
        val capabilities = UserCapabilitiesInteractorImpl(api, provider, schedulers)
        val observer = VerifyCodeInteractorImpl(api, capabilities, schedulers).verifyCode("request", "123456", null).test()
        pending.onError(java.io.IOException("offline"))
        observer.assertValue(login).assertComplete()
        assertNull(provider.getCapabilities())
    }

    @Test
    fun `rejected login preserves the current session capabilities`() {
        val provider = UserCapabilitiesProviderImpl().apply { setCapabilities(guest) }
        val api = api(Single.error(IllegalArgumentException("invalid code")), Single.never())
        val capabilities = UserCapabilitiesInteractorImpl(api, provider, schedulers)
        VerifyCodeInteractorImpl(api, capabilities, schedulers).verifyCode("request", "bad", null)
            .test().assertError(IllegalArgumentException::class.java)
        assertEquals(guest, provider.getCapabilities())
    }

    private fun api(
        verify: Single<StoreResponse<VerifyCodeResponse>>,
        capabilities: Single<StoreResponse<UserCapabilitiesResponse>>,
    ): StoreApi = Proxy.newProxyInstance(StoreApi::class.java.classLoader, arrayOf(StoreApi::class.java)) { _, method, _ ->
        when (method.name) {
            "verifyCode" -> verify
            "getUserCapabilities" -> capabilities
            else -> error("Unexpected API call: ${method.name}")
        }
    } as StoreApi
}
