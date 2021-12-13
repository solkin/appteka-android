package com.tomclaw.appsend.screen.topics

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.TopicEntry
import com.tomclaw.appsend.net.UserData
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface TopicsInteractor {

    fun listTopics(offset: Int = 0): Observable<List<TopicEntry>>

}

class TopicsInteractorImpl(
    private val userData: UserData,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : TopicsInteractor {

    override fun listTopics(offset: Int): Observable<List<TopicEntry>> {
        return api
            .getTopicsList(
                guid = userData.guid,
                offset = offset
            )
            .map { it.result.topics }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
