package com.tomclaw.appsend.screen.moderation

import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface ModerationInteractor {

    fun listApps(): Observable<List<AppEntity>>

}

class ModerationInteractorImpl(
    private val schedulers: SchedulersFactory
) : ModerationInteractor {

    override fun listApps(): Observable<List<AppEntity>> {
        return Single.create<List<AppEntity>> { source ->
            source.onSuccess(emptyList())
        }.toObservable()
            .subscribeOn(schedulers.io())
    }

}