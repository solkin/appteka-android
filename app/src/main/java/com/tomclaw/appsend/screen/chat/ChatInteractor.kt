package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.chat.api.ReadTopicResponse
import com.tomclaw.appsend.screen.chat.api.ReportMessageResponse
import com.tomclaw.appsend.screen.chat.api.SendMessageResponse
import com.tomclaw.appsend.screen.topics.api.PinTopicResponse
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.StringUtil
import io.reactivex.rxjava3.core.Observable

interface ChatInteractor {

    fun getUserBrief(): Observable<UserBrief>

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
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ChatInteractor {

    override fun getUserBrief(): Observable<UserBrief> {
        return api
            .getUserBrief(userId = null)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun getTopic(topicId: Int): Observable<TopicEntity> {
        return api.getTopicInfo(topicId = topicId)
            .map { it.result.topic }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun loadHistory(
        topicId: Int,
        fromId: Int,
        tillId: Int
    ): Observable<List<MessageEntity>> {
        return api
            .getChatHistory(
                topicId = topicId,
                from = fromId,
                till = tillId
            )
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
        return api
            .sendMessage(
                topicId = topicId,
                text = text,
                attachment = attachment,
                cookie = cookie,
            )
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun reportMessage(msgId: Int): Observable<ReportMessageResponse> {
        return api.reportMessage(msgId = msgId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun readTopic(topicId: Int, msgId: Int): Observable<ReadTopicResponse> {
        return api.readTopic(topicId = topicId, msgId = msgId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

    override fun pinTopic(topicId: Int): Observable<PinTopicResponse> {
        return api
            .pinTopic(topicId = topicId)
            .map { it.result }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}
