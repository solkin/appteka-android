package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.dto.UserData
import com.tomclaw.appsend.screen.chat.api.ReadTopicResponse
import com.tomclaw.appsend.screen.chat.api.ReportMessageResponse
import com.tomclaw.appsend.screen.chat.api.SendMessageResponse
import com.tomclaw.appsend.screen.topics.api.PinTopicResponse
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.StringUtil
import io.reactivex.rxjava3.core.Observable

interface ChatInteractor {

    fun getUserData(): Observable<UserData>

    fun getTopic(topicId: Int): Observable<TopicEntity>

    fun loadHistory(topicId: Int, fromId: Int, tillId: Int): Observable<List<MessageEntity>>

    fun sendMessage(
        topicId: Int,
        text: String?,
        attachment: String?
    ): Observable<SendMessageResponse>

    fun reportMessage(msgId: Int): Observable<ReportMessageResponse>

    fun readTopic(topicId: Int, msgId: Int): Observable<ReadTopicResponse>

    fun pinTopic(topicId: Int): Observable<PinTopicResponse>

}

class ChatInteractorImpl(
    private val userDataInteractor: UserDataInteractor,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ChatInteractor {

    override fun getUserData(): Observable<UserData> {
        return userDataInteractor
            .getUserData()
            .toObservable()
            .subscribeOn(schedulers.io())
    }

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

    override fun sendMessage(
        topicId: Int,
        text: String?,
        attachment: String?
    ): Observable<SendMessageResponse> {
        val cookie = StringUtil.generateCookie()
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.sendMessage(
                    guid = it.guid,
                    topicId = topicId,
                    text = text,
                    attachment = attachment,
                    cookie = cookie,
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun reportMessage(msgId: Int): Observable<ReportMessageResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.reportMessage(
                    guid = it.guid,
                    msgId = msgId,
                )
            }
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun readTopic(topicId: Int, msgId: Int): Observable<ReadTopicResponse> {
        return userDataInteractor
            .getUserData()
            .flatMap {
                api.readTopic(
                    guid = it.guid,
                    topicId = topicId,
                    msgId = msgId,
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
