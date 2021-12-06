package com.tomclaw.appsend.screen.topics

import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.topics.api.TopicEntry
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.concurrent.TimeUnit

interface TopicsInteractor {

    fun listTopics(offset: Int = 0): Observable<List<TopicEntry>>

}

class TopicsInteractorImpl(
    private val schedulers: SchedulersFactory
) : TopicsInteractor {

    private var counter = 0

    override fun listTopics(offset: Int): Observable<List<TopicEntry>> {
        return Single
            .create<List<TopicEntry>> { emitter ->
                val firstList = listOf(
                    TopicEntry(
                        topicId = 1,
                        type = 0,
                        icon = "",
                        title = "",
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
                            msgId = 1,
                            prevMsgId = 0,
                            time = System.currentTimeMillis() / 1000,
                            cookie = "",
                            type = 0,
                            text = "Lorem ipsum dolor",
                            attachment = null,
                            incoming = true,
                        ),
                    )
                )
                val secondList = listOf(
                    TopicEntry(
                        topicId = 2,
                        type = 0,
                        icon = "https://appteka.store/api/1/icon/get?hash=WlZZvscYs6xbZfv7KOFetvLEjBtt76HEfC60QzDgmKk57jmc9xHmW9HV0xIslsII",
                        title = "Music Pro Player",
                        description = null,
                        packageName = "com.arobaZone.musicplayer",
                        isPinned = false,
                        readMsgId = null,
                        lastMsg = MessageEntity(
                            userId = 328570,
                            userIcon = UserIcon(
                                icon = "<svg height=\"24\" viewBox=\"0 0 24 24\" width=\"24\" xmlns=\"http://www.w3.org/2000/svg\" fill=\"white\"><path d=\"m16 12h-8v-6h8m.67-2h-1.67v-2h-6v2h-1.67a1.33 1.33 0 0 0 -1.33 1.33v15.34c0 .73.6 1.33 1.33 1.33h9.34a1.33 1.33 0 0 0 1.33-1.33v-15.34c0-.73-.6-1.33-1.33-1.33z\"/></svg>",
                                label = mapOf(Pair("en", "Battery"), Pair("ru", "Батарейка")),
                                color = "#fb8a36"
                            ),
                            topicId = 2,
                            msgId = 2,
                            prevMsgId = 0,
                            time = System.currentTimeMillis() / 1000,
                            cookie = "",
                            type = 0,
                            text = "Lorem ipsum dolor",
                            attachment = null,
                            incoming = true,
                        ),
                    )
                )
                val list = when (offset) {
                    0 -> firstList
                    1 -> {
                        if (counter++ < 2) {
                            throw RuntimeException()
                        }
                        secondList
                    }
                    else -> emptyList()
                }
                emitter.onSuccess(list)
            }
            .toObservable()
            .delay(1, TimeUnit.SECONDS)
            .subscribeOn(schedulers.io())
    }

}