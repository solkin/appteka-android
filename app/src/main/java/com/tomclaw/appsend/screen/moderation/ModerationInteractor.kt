package com.tomclaw.appsend.screen.moderation

import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

interface ModerationInteractor {

    fun listApps(): Observable<List<AppEntity>>

}

class ModerationInteractorImpl(
    private val schedulers: SchedulersFactory
) : ModerationInteractor {

    override fun listApps(): Observable<List<AppEntity>> {
        return Single.create<List<AppEntity>> { source ->
            val list = listOf(AppEntity(1, null, "AppSend", "1.0", 1, 100000, 5.0f, 100))
//            val list = emptyList<AppEntity>()
            source.onSuccess(list)
        }.delay(1, TimeUnit.SECONDS)
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}