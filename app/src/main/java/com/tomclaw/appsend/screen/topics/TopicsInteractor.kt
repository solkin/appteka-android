package com.tomclaw.appsend.screen.topics

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.topics.api.PinTopicResponse
import com.tomclaw.appsend.screen.topics.api.TopicsResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface TopicsInteractor {

    fun listTopics(offset: Int = 0): Observable<TopicsResponse>

    fun pinTopic(topicId: Int): Observable<PinTopicResponse>

}

class TopicsInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : TopicsInteractor {

    override fun listTopics(offset: Int): Observable<TopicsResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.getTopicsList(
                    guid = it.guid,
                    offset = offset
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun pinTopic(topicId: Int): Observable<PinTopicResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.pinTopic(
                    guid = it.guid,
                    topicId = topicId,
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
