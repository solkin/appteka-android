package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.profile.api.Profile
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.SingleSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.lang.reflect.Proxy

class ProfileInteractorTest {
    private val schedulers = object : SchedulersFactory {
        override fun io(): Scheduler = Schedulers.trampoline()
        override fun mainThread(): Scheduler = Schedulers.trampoline()
    }
    private val icon = UserIcon("", color = "")

    @Test
    fun `explicit profile uses current viewer identity on every load`() {
        var viewerId = 7
        val api = api { method, args ->
            when (method) {
                "getProfile" -> Single.just(StoreResponse(200, ProfileResponse(Profile(userId = args[0] as Int, userIcon = icon)), ""))
                "getUserBrief" -> {
                    assertEquals(null, args[0])
                    Single.just(StoreResponse(200, brief(viewerId), ""))
                }
                else -> error(method)
            }
        }
        val interactor = ProfileInteractorImpl(api, schedulers)
        assertEquals(true, interactor.loadProfile(7).blockingFirst().isSelf)
        viewerId = 8
        assertEquals(false, interactor.loadProfile(7).blockingFirst().isSelf)
    }

    @Test
    fun `own profile route uses its authenticated server response without an extra identity request`() {
        val api = api { method, args ->
            check(method == "getProfile")
            assertEquals(null, args[0])
            Single.just(StoreResponse(200, ProfileResponse(Profile(userId = 7, userIcon = icon)), ""))
        }
        assertEquals(true, ProfileInteractorImpl(api, schedulers).loadProfile(null).blockingFirst().isSelf)
    }

    @Test
    fun `identity errors propagate and cancel the profile request`() {
        val profile = SingleSubject.create<StoreResponse<ProfileResponse>>()
        val viewer = SingleSubject.create<StoreResponse<UserBrief>>()
        val interactor = ProfileInteractorImpl(api { method, _ ->
            when (method) {
                "getProfile" -> profile
                "getUserBrief" -> viewer
                else -> error(method)
            }
        }, schedulers)
        val observer = interactor.loadProfile(7).test()
        viewer.onError(java.io.IOException("offline"))
        observer.assertError(java.io.IOException::class.java)
        assertFalse(profile.hasObservers())
    }

    private fun brief(id: Int) = UserBrief(id, icon, 0, 0, 0, null, false, false, null)

    private fun api(call: (String, Array<out Any?>) -> Any): StoreApi =
        Proxy.newProxyInstance(StoreApi::class.java.classLoader, arrayOf(StoreApi::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as StoreApi
}
