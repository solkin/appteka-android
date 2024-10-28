package com.tomclaw.appsend.screen.subscribers

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.subscribers.api.SubscriberEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface SubscribersInteractor {

    fun listSubscribers(offsetId: Int? = null): Observable<List<SubscriberEntity>>

}

class SubscribersInteractorImpl(
    private val api: StoreApi,
    private val userId: Int,
    private val schedulers: SchedulersFactory
) : SubscribersInteractor {

    override fun listSubscribers(offsetId: Int?): Observable<List<SubscriberEntity>> {
        return api
            .getSubscribersList(
                userId = userId,
                rowId = offsetId,
            )
            .map { list ->
                list.result.entries
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
