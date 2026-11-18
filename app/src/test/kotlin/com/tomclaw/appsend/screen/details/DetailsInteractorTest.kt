package com.tomclaw.appsend.screen.details

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.bananalytics.Bananalytics
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.lang.reflect.Proxy
import java.util.Locale

class DetailsInteractorTest {

    // What each request named: ref, else package.
    private val requests = mutableListOf<String?>()

    @Test
    fun `an edition the store does not have opens the package page`() {
        val interactor = interactor { _, _, ref ->
            if (ref != null) Single.error(httpError(404)) else Single.just(response())
        }

        interactor.loadDetails(appId = null, packageName = "app", ref = "app/aaaaaaaaaaaaaaaaaaaaaaaa")
            .test()
            .assertValueCount(1)

        assertEquals(listOf("app/aaaaaaaaaaaaaaaaaaaaaaaa", "app"), requests)
    }

    @Test
    fun `a release address without a package has nothing to fall back to`() {
        val interactor = interactor { _, _, _ -> Single.error(httpError(404)) }

        interactor.loadDetails(appId = null, packageName = null, ref = "app/aaaaaaaaaaaaaaaaaaaaaaaa/1")
            .test()
            .assertError(HttpException::class.java)

        assertEquals(1, requests.size)
    }

    @Test
    fun `other failures are not taken for a missing edition`() {
        val interactor = interactor { _, _, _ -> Single.error(httpError(500)) }

        interactor.loadDetails(appId = null, packageName = "app", ref = "app/aaaaaaaaaaaaaaaaaaaaaaaa")
            .test()
            .assertError(HttpException::class.java)

        assertEquals(1, requests.size)
    }

    private fun interactor(
        getInfo: (appId: String?, packageName: String?, ref: String?) -> Single<StoreResponse<Details>>,
    ): DetailsInteractor {
        val api = proxy<StoreApi> { method, args ->
            check(method == "getInfo") { "unexpected $method" }
            val ref = args[2] as String?
            requests += ref ?: args[1] as String?
            getInfo(args[0] as String?, args[1] as String?, ref)
        }
        val schedulers = object : SchedulersFactory {
            override fun io(): Scheduler = Schedulers.trampoline()
            override fun mainThread(): Scheduler = Schedulers.trampoline()
        }
        return DetailsInteractorImpl(proxy<Bananalytics> { _, _ -> null }, api, Locale.ENGLISH, schedulers)
    }

    private fun response() = StoreResponse(status = 200, result = testDetails(), description = "")

    private fun httpError(code: Int) = HttpException(
        Response.error<Any>(code, "".toResponseBody(null))
    )

    private inline fun <reified T : Any> proxy(crossinline answer: (String, Array<Any?>) -> Any?): T =
        T::class.java.cast(
            Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
                answer(method.name, args ?: emptyArray())
            }
        ) as T

}
