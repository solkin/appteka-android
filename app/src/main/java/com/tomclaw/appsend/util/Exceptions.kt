package com.tomclaw.appsend.util

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tomclaw.appsend.core.permissions.Capability
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

fun Throwable.filterUnauthorizedErrors(authError: () -> Unit, other: (ex: Throwable) -> Unit) {
    if (this is HttpException && code() == 401) {
        authError()
        return
    }
    other(this)
}

/**
 * Whether [this] is an [HttpException] with the given status [code].
 * Useful for treating expected client-side responses (e.g. 404 for
 * "no such app in catalog") as a normal UI state instead of a crash.
 */
fun Throwable.isHttpError(code: Int): Boolean = this is HttpException && code() == code

/**
 * Extension of [filterUnauthorizedErrors] that also recognises a
 * structured capability-denied response (sent by the server with
 * `reason` / `blocked_by` / `hint_key` fields). Layout:
 *   - 401 → [authError]
 *   - any 4xx whose JSON body parses as a [Capability] denial → [capabilityDenied]
 *   - everything else → [other]
 *
 * The capability is delivered as a fully-formed [Capability] with
 * `allowed = false`, so callers can pass it to the same hint resolver
 * they use for proactive UI without an extra translation step.
 */
fun Throwable.filterCapabilityErrors(
    authError: () -> Unit,
    capabilityDenied: (Capability) -> Unit,
    other: (ex: Throwable) -> Unit,
) {
    if (this is HttpException) {
        val code = code()
        if (code == 401) {
            authError()
            return
        }
        if (code in 400..499) {
            val capability = parseCapabilityDenial(this)
            if (capability != null) {
                capabilityDenied(capability)
                return
            }
        }
    }
    other(this)
}

private val denialGson = Gson()

private fun parseCapabilityDenial(ex: HttpException): Capability? {
    // errorBody() may be consumed at most once; we read it eagerly
    // here, so any subsequent `other(...)` callers should treat the
    // exception as already-inspected. None of the current call sites
    // touch the body again, so this is fine in practice.
    val body = try {
        ex.response()?.errorBody()?.string()
    } catch (e: Exception) {
        null
    } ?: return null
    return try {
        val parsed = denialGson.fromJson(body, CapabilityDenialBody::class.java)
        // Only treat as a capability denial when the server actually
        // shipped one of the reason fields. A plain 400 ({status,
        // description}) must fall through to `other`.
        if (parsed?.reason == null && parsed?.blockedBy == null && parsed?.hintKey == null) {
            return null
        }
        Capability(
            allowed = false,
            reason = parsed.reason,
            blockedBy = parsed.blockedBy,
            hintKey = parsed.hintKey,
        )
    } catch (e: JsonSyntaxException) {
        null
    }
}

/**
 * Mirror of httputil.ErrorResult on the server. Field names use
 * snake_case via [com.google.gson.annotations.SerializedName] only
 * where the JSON name differs from the Kotlin property name.
 */
private data class CapabilityDenialBody(
    val status: Int? = null,
    val description: String? = null,
    val reason: String? = null,
    @com.google.gson.annotations.SerializedName("blocked_by")
    val blockedBy: String? = null,
    @com.google.gson.annotations.SerializedName("hint_key")
    val hintKey: String? = null,
)

/**
 * Retries the upstream on transient failures only. Any 4xx HTTP
 * response is propagated as a permanent error — the server is telling
 * us the request itself is wrong (auth missing, forbidden by ACL, not
 * found, validation), and re-sending the same payload will not change
 * that. 5xx and non-HTTP throwables (network, IO) are still retried
 * with the configured delay.
 *
 * Earlier this only treated 401 as permanent; everything else looped
 * forever, which masked capability-denied (400/403) responses behind
 * a never-ending spinner.
 */
fun <T : Any> Observable<T>.retryWhenNonAuthErrors(
    delay: Long = 3
): Observable<T> {
    return retryWhen { errors ->
        errors.flatMap { ex ->
            if (ex is HttpException && ex.code() in 400..499) {
                Single.create<T> { it.onError(ex) }.toObservable()
            } else {
                println("Retry after exception: " + ex.message)
                Observable.timer(delay, TimeUnit.SECONDS)
            }
        }
    }
}
