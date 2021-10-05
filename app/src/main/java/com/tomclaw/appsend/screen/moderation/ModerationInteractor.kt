package com.tomclaw.appsend.screen.moderation

import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

interface ModerationInteractor {

    fun listApps(offsetAppId: Int = 0): Observable<List<AppEntity>>

}

class ModerationInteractorImpl(
    private val schedulers: SchedulersFactory
) : ModerationInteractor {

    override fun listApps(offsetAppId: Int): Observable<List<AppEntity>> {
        return Single.create<List<AppEntity>> { source ->
            val list = ArrayList<AppEntity>()
            for (i in 1..5) {
                val id = offsetAppId + i
                list.add(AppEntity(id, null, "App $id", "1.0", 1, 100000, 5.0f, 100))
            }
//            val list = emptyList<AppEntity>()
            source.onSuccess(list)
        }.delay(1, TimeUnit.SECONDS)
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}