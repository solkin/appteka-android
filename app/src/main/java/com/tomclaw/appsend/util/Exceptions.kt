package com.tomclaw.appsend.util

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

fun <T : Any> Observable<T>.retryWhenNonAuthErrors(
    delay: Long = 3
): Observable<T> {
    return retryWhen { errors ->
        errors.flatMap { ex ->
            if (ex is HttpException && ex.code() == 401) {
                Single.create<T> { it.onError(ex) }.toObservable()
            } else {
                println("Retry after exception: " + ex.message)
                Observable.timer(delay, TimeUnit.SECONDS)
            }
        }
    }
}
