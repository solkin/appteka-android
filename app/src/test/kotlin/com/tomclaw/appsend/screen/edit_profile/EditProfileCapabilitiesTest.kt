package com.tomclaw.appsend.screen.edit_profile

import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.UserCapabilitiesInteractor
import com.tomclaw.appsend.core.permissions.UserCapabilitiesProviderImpl
import com.tomclaw.appsend.core.permissions.api.UserCapabilitiesResponse
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.profile.api.Profile
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.Proxy

class EditProfileCapabilitiesTest {
    @Test
    fun `live rights changes update editors while preserving unsaved text and detach stops rendering`() {
        val provider = UserCapabilitiesProviderImpl()
        val names = PublishSubject.create<String>()
        val bios = PublishSubject.create<String>()
        val calls = mutableListOf<String>()
        val fields = mutableMapOf<String, String>()
        val view = stub<EditProfileView> { method, args ->
            when (method) {
                "nameChanged" -> names
                "bioChanged" -> bios
                "navigationClicks", "changeAvatarClicks", "removeAvatarClicks", "saveClicks" -> Observable.never<Unit>()
                else -> {
                    calls += method
                    if (method == "setName" || method == "setBio") fields[method] = args[0] as String
                    null
                }
            }
        }
        val interactor = object : EditProfileInteractor {
            override fun loadProfile() = Observable.just(ProfileResponse(Profile(userIcon = UserIcon("", color = ""), name = "Initial", bio = "Initial bio")))
            override fun updateProfile(request: EditProfileRequest): Observable<ProfileResponse> = error("No save expected")
        }
        val capabilities = object : UserCapabilitiesInteractor {
            override fun refresh(): Single<UserCapabilitiesResponse> = Single.never()
            override fun reset() = error("No session change expected")
        }
        val schedulers = object : SchedulersFactory {
            override fun io(): Scheduler = Schedulers.trampoline()
            override fun mainThread(): Scheduler = Schedulers.trampoline()
        }
        val presenter = EditProfilePresenterImpl(interactor, stub { _, _ -> "unused" }, provider, capabilities, schedulers, null)
        presenter.attachView(view)
        names.onNext("Draft")
        bios.onNext("Draft bio")
        provider.setCapabilities(mapOf(CapabilityAction.BIO_EDIT to Capability(true), CapabilityAction.AVATAR_UPLOAD to Capability(true)))
        assertEquals(true, calls.contains("showAvatarEditor"))
        assertEquals(true, calls.contains("showBioEditor"))
        assertEquals("Draft", fields["setName"])
        assertEquals("Draft bio", fields["setBio"])
        calls.clear()
        provider.setCapabilities(mapOf(CapabilityAction.BIO_EDIT to Capability(false), CapabilityAction.AVATAR_UPLOAD to Capability(false)))
        assertEquals(true, calls.contains("hideAvatarEditor"))
        assertEquals(true, calls.contains("hideBioEditor"))
        assertEquals("Draft bio", fields["setBio"])
        presenter.detachView()
        calls.clear()
        provider.clear()
        assertEquals(emptyList<String>(), calls)
    }

    private inline fun <reified T> stub(crossinline call: (String, Array<out Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            call(method.name, args ?: emptyArray())
        } as T
}
