package com.tomclaw.appsend.events

import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface EventsInteractor {

    fun subscribeOnEvents(): Observable<Unit>

}

class EventsInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : EventsInteractor {

    private val eventsRelay = PublishRelay.create<Unit>()

    private fun eventsLoop() {
        userDataInteractor.getUserData()
//            .flatMap {
//                it.guid
//            }
            .subscribeOn(schedulers.io())
        eventsRelay.hasObservers()
    }

    override fun subscribeOnEvents(): Observable<Unit> {
        return eventsRelay
            .doOnSubscribe {
                println("Subscribe")
            }
            .doOnDispose {
                println("Dispose: " + eventsRelay.hasObservers())
            }
    }

}
