package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable

interface ChatInteractor {

    fun getTopic(topicId: Int): Observable<TopicEntity>

    fun loadHistory(topicId: Int, fromId: Int, tillId: Int): Observable<List<MessageEntity>>

}

class ChatInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ChatInteractor {

    override fun getTopic(topicId: Int): Observable<TopicEntity> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.getTopicInfo(
                    guid = it.guid,
                    topicId = topicId
                )
            }
            .map { it.result.topic }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadHistory(
        topicId: Int,
        fromId: Int,
        tillId: Int
    ): Observable<List<MessageEntity>> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.getChatHistory(
                    guid = it.guid,
                    topicId = topicId,
                    from = fromId,
                    till = tillId
                )
            }
            .map { it.result.messages.sortedBy { msg -> msg.msgId } }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
