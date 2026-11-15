package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.permissions.UserCapabilitiesProviderImpl
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.profile.api.Profile
import com.tomclaw.appsend.screen.profile.api.UserAppsResponse
import com.tomclaw.appsend.user.ModerationProviderImpl
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import dagger.Lazy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Test
import java.lang.reflect.Proxy

class ProfileSelfTest {
    @Test
    fun `own explicit profile uses self controls and cannot subscribe to itself`() {
        val fixture = Fixture(7)
        fixture.presenter.attachView(fixture.view)
        assertEquals(true, fixture.self)
        fixture.presenter.onSubscribeClick()
        fixture.presenter.onUnsubscribeClick()
        assertEquals(0, fixture.subscriptionCalls)
        fixture.presenter.detachView()
    }

    @Test
    fun `a different user keeps subscription controls and actions`() {
        val fixture = Fixture(8)
        fixture.presenter.attachView(fixture.view)
        assertEquals(false, fixture.self)
        fixture.presenter.onSubscribeClick()
        fixture.presenter.onUnsubscribeClick()
        assertEquals(2, fixture.subscriptionCalls)
        fixture.presenter.detachView()
    }

    @Test
    fun `authorization cancels the previous viewer request and ignores its late response`() {
        val guest = PublishSubject.create<ProfileData>()
        val authorized = PublishSubject.create<ProfileData>()
        val fixture = Fixture(7, listOf(guest, authorized))
        fixture.presenter.attachView(fixture.view)
        fixture.presenter.onAuthorized()
        assertFalse(guest.hasObservers())
        val target = Profile(userId = 7, userIcon = UserIcon("", color = ""))
        authorized.onNext(ProfileData(target, viewerId = 7))
        guest.onNext(ProfileData(target, viewerId = 8))
        assertEquals(true, fixture.self)
        fixture.presenter.detachView()
        assertFalse(authorized.hasObservers())
    }

    @Test
    fun `authorization drops the old viewer identity before the new profile arrives`() {
        val target = Profile(userId = 7, userIcon = UserIcon("", color = ""))
        val pending = PublishSubject.create<ProfileData>()
        val fixture = Fixture(7, listOf(Observable.just(ProfileData(target, viewerId = 8)), pending))
        fixture.presenter.attachView(fixture.view)
        fixture.presenter.onAuthorized()
        fixture.presenter.onSubscribeClick()
        assertEquals(0, fixture.subscriptionCalls)
        fixture.presenter.detachView()
    }

    @Test
    fun `failed identity load shows the error and retry reloads the profile`() {
        val target = Profile(userId = 7, userIcon = UserIcon("", color = ""))
        val fixture = Fixture(7, listOf(Observable.error(java.io.IOException("offline")), Observable.just(ProfileData(target, viewerId = 7))))
        fixture.presenter.attachView(fixture.view)
        assertEquals(1, fixture.errors)
        fixture.retries.onNext(Unit)
        assertEquals(true, fixture.self)
        fixture.presenter.detachView()
    }

    private class Fixture(target: Int, responses: List<Observable<ProfileData>> = emptyList()) {
        private var loads = 0
        var self: Boolean? = null
        var errors = 0
        val retries = PublishSubject.create<Unit>()
        var subscriptionCalls = 0
        val view = stub<ProfileView> { method, _ ->
            when {
                method == "retryClicks" -> retries
                method.endsWith("Clicks") || method == "swipeRefresh" -> Observable.never<Any>()
                method == "showError" -> { errors++; null }
                else -> null
            }
        }
        val presenter = ProfilePresenterImpl(
            userId = target,
            withToolbar = true,
            interactor = stub<ProfileInteractor> { method, _ ->
                when (method) {
                    "loadProfile" -> responses.getOrNull(loads++) ?: Observable.just(ProfileData(Profile(userId = target, userIcon = UserIcon("", color = ""), isRegistered = true), viewerId = 7))
                    "loadUserApps" -> Observable.just(UserAppsResponse(emptyList()))
                    "subscribe", "unsubscribe" -> { subscriptionCalls++; Observable.never<Any>() }
                    else -> error("Unexpected interactor call: $method")
                }
            },
            converter = stub<ProfileConverter> { method, args ->
                check(method == "convertProfile")
                self = args[3] as Boolean
                emptyList<Any>()
            },
            moderationProvider = ModerationProviderImpl(),
            capabilitiesProvider = UserCapabilitiesProviderImpl(),
            adapterPresenter = Lazy { stub<AdapterPresenter> { method, _ -> check(method == "onDataSourceChanged"); null } },
            schedulers = object : SchedulersFactory {
                override fun io(): Scheduler = Schedulers.trampoline()
                override fun mainThread(): Scheduler = Schedulers.trampoline()
            },
            state = null,
        )
    }
}

private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
        call(method.name, args ?: emptyArray())
    } as T
