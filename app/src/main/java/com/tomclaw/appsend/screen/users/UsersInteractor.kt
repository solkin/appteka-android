package com.tomclaw.appsend.screen.users

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.screen.users.api.UserEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface UsersInteractor {

    fun listUsers(userId: Int, offsetId: Int? = null): Observable<List<UserEntity>>

}

class SubscribersInteractor(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : UsersInteractor {

    override fun listUsers(userId: Int, offsetId: Int?): Observable<List<UserEntity>> {
        return api
            .getSubscribersList(
                userId = userId,
                rowId = offsetId,
            )
            .map { list ->
                list.result.entries as List<UserEntity>
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}

class PublishersInteractor(
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : UsersInteractor {

    override fun listUsers(userId: Int, offsetId: Int?): Observable<List<UserEntity>> {
        return api
            .getSubscribersList(
                userId = userId,
                rowId = offsetId,
            )
            .map { list ->
                list.result.entries as List<UserEntity>
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
