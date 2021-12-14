package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntry
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.net.UserData
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

interface ChatInteractor {

    fun getTopic(topicId: Int): Observable<TopicEntry>

    fun loadHistory(topicId: Int, fromId: Int, tillId: Int): Observable<List<MessageEntity>>

}

class ChatInteractorImpl(
    private val userData: UserData,
    private val api: StoreApi,
    private val schedulers: SchedulersFactory
) : ChatInteractor {

    override fun getTopic(topicId: Int): Observable<TopicEntry> {
        return Single
            .create<TopicEntry> { emitter ->
                emitter.onSuccess(
                    TopicEntry(
                        topicId = 1,
                        type = 0,
                        icon = "",
                        title = "Common questions",
                        description = null,
                        packageName = null,
                        isPinned = true,
                        readMsgId = null,
                        lastMsg = MessageEntity(
                            userId = 328575,
                            userIcon = UserIcon(
                                icon = "<svg height=\"24\" viewBox=\"0 0 24 24\" width=\"24\" xmlns=\"http://www.w3.org/2000/svg\" fill=\"white\"><path d=\"m19.59 3h2.41v2h-1.59l-4.24 4.24c-.37-.56-.85-1.04-1.41-1.41zm-7.59 5a4 4 0 0 1 4 4c0 1.82-1.23 3.42-3 3.87v.13a5 5 0 0 1 -5 5 5 5 0 0 1 -5-5 5 5 0 0 1 5-5h.13c.45-1.76 2.04-3 3.87-3m0 2.5a1.5 1.5 0 0 0 -1.5 1.5 1.5 1.5 0 0 0 1.5 1.5 1.5 1.5 0 0 0 1.5-1.5 1.5 1.5 0 0 0 -1.5-1.5m-5.06 3.74-.71.7 2.83 2.83.71-.71z\"/></svg>",
                                label = mapOf(Pair("en", "Battery"), Pair("ru", "Батарейка")),
                                color = "#9742c7"
                            ),
                            topicId = 1,
                            msgId = 101,
                            prevMsgId = 100,
                            time = System.currentTimeMillis() / 1000,
                            cookie = "",
                            type = 0,
                            text = "Lorem ipsum dolor",
                            attachment = null,
                            incoming = true,
                        ),
                    )
                )
            }
            .toObservable()
            .delay(200, TimeUnit.MILLISECONDS)
            .subscribeOn(schedulers.io())
    }

    override fun loadHistory(
        topicId: Int,
        fromId: Int,
        tillId: Int
    ): Observable<List<MessageEntity>> {
        return api
            .getChatHistory(
                guid = userData.guid,
                topicId = topicId,
                from = fromId,
                till = tillId
            )
            .map { it.result.messages.sortedBy { msg -> msg.msgId } }
            .toObservable()
    }

}
