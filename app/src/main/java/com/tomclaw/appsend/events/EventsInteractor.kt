package com.tomclaw.appsend.events

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong


interface EventsInteractor {

    fun subscribeOnEvents(): Observable<EventsResponse>

}

class EventsInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : EventsInteractor {

    private val eventsSubject = PublishSubject.create<EventsResponse>()
    private var observers = AtomicInteger(0)

    private val fetchTime = AtomicLong(0)

    private val disposables = CompositeDisposable()

    private fun eventsLoop() {
        disposables += userDataInteractor.getUserData().toObservable()
            .flatMap {
                println("[polling] started with time " + fetchTime.get())
                api.getEvents(guid = it.guid, time = fetchTime.get(), noDelay = false)
                    .toObservable()
                    .takeUntil(Observable.timer(1, MINUTES))
            }
            .observeOn(schedulers.io())
            .subscribeOn(schedulers.mainThread())
            .retryWhen { errors ->
                errors.flatMap {
                    println("[polling] Retry after exception: " + it.message)
                    Observable.timer(5, SECONDS)
                }
            }
            .repeat()
            .subscribe(
                { result ->
                    fetchTime.set(result.result.time)
                    println("[polling] result received with time " + result.result.time)
                    eventsSubject.onNext(result.result)
                }, { ex ->
                    println("[polling] Exception: " + ex.message)
                }
            )
    }

    override fun subscribeOnEvents(): Observable<EventsResponse> {
        return eventsSubject
            .doOnSubscribe {
                println("[polling] subscribe")
                if (observers.incrementAndGet() == 1) {
                    eventsLoop()
                }
            }
            .doOnDispose {
                println("[polling] dispose")
                if (observers.decrementAndGet() == 0) {
                    println("[polling] stop polling")
                    disposables.clear()
                }
            }
    }

}
