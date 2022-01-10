package com.tomclaw.appsend.events

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


interface EventsInteractor {

    fun subscribeOnEvents(): Observable<Unit>

}

class EventsInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : EventsInteractor {

    private val eventsSubject = PublishSubject.create<Unit>()
    private var observers = AtomicInteger(0)

    private val disposables = CompositeDisposable()

    private fun eventsLoop() {
        disposables += Observable
            .interval(0, 5, TimeUnit.SECONDS)
            .flatMap { userDataInteractor.getUserData().toObservable() }
            .flatMap {
                api.getTopicsList(it.guid, 0)
                    .toObservable()
                    .takeUntil(Observable.timer(5, TimeUnit.SECONDS))
            }
            .observeOn(schedulers.io())
            .subscribe(
                { result ->
                    // Use result, example below.
                    println("Polling result received")
                    eventsSubject.onNext(Unit)
                }, { throwable: Throwable? ->
                    println(throwable)
                }
            )
    }

    override fun subscribeOnEvents(): Observable<Unit> {
        return eventsSubject
            .doOnSubscribe {
                println("Subscribe")
                if (observers.incrementAndGet() == 1) {
                    eventsLoop()
                }
            }
            .doOnDispose {
                println("Dispose")
                if (observers.decrementAndGet() == 0) {
                    println("Stop polling")
                    disposables.clear()
                }
            }
    }

}
