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
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
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

    @Test
    fun `guest can load an explicit profile without self controls`() {
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
        viewer.onError(httpError(401))
        observer.assertNoValues().assertNotComplete()
        assertEquals(true, profile.hasObservers())
        profile.onSuccess(StoreResponse(200, ProfileResponse(Profile(userId = 7, userIcon = icon)), ""))
        observer.assertValue { it.profile.userId == 7 && !it.isSelf }.assertComplete()
    }

    @Test
    fun `viewer identity is refreshed across guest login and logout`() {
        var signedIn = false
        val interactor = ProfileInteractorImpl(api { method, _ ->
            when (method) {
                "getProfile" -> Single.just(StoreResponse(200, ProfileResponse(Profile(userId = 7, userIcon = icon)), ""))
                "getUserBrief" -> if (signedIn) {
                    Single.just(StoreResponse(200, brief(7), ""))
                } else {
                    Single.error<StoreResponse<UserBrief>>(httpError(401))
                }
                else -> error(method)
            }
        }, schedulers)
        assertFalse(interactor.loadProfile(7).blockingFirst().isSelf)
        signedIn = true
        assertEquals(true, interactor.loadProfile(7).blockingFirst().isSelf)
        signedIn = false
        assertFalse(interactor.loadProfile(7).blockingFirst().isSelf)
    }

    @Test
    fun `own profile still requires authorization`() {
        val error = httpError(401)
        val interactor = ProfileInteractorImpl(api { method, _ ->
            check(method == "getProfile")
            Single.error<StoreResponse<ProfileResponse>>(error)
        }, schedulers)
        interactor.loadProfile(null).test().assertError(error)
    }

    @Test
    fun `profile failures are not treated as a missing viewer`() {
        for (code in listOf(401, 403, 404, 500)) {
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
            val error = httpError(code)
            profile.onError(error)
            observer.assertError(error).assertNoValues()
            assertFalse(viewer.hasObservers())
        }
    }

    @Test
    fun `non authorization viewer failures propagate and cancel the profile request`() {
        for (code in listOf(403, 404, 429, 500)) {
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
            val error = httpError(code)
            viewer.onError(error)
            observer.assertError(error).assertNoValues()
            assertFalse(profile.hasObservers())
        }
    }

    @Test
    fun `disposing cancels both pending requests`() {
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
        observer.dispose()
        assertFalse(profile.hasObservers())
        assertFalse(viewer.hasObservers())
        observer.assertNoValues()
    }

    private fun httpError(code: Int) = HttpException(Response.error<Any>(code, "".toResponseBody()))

    private fun brief(id: Int) = UserBrief(id, icon, 0, 0, 0, null, false, false, null)

    private fun api(call: (String, Array<out Any?>) -> Any): StoreApi =
        Proxy.newProxyInstance(StoreApi::class.java.classLoader, arrayOf(StoreApi::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as StoreApi
}
