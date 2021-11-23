package com.tomclaw.appsend.screen.discuss

import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.discuss.api.MessageEntry
import com.tomclaw.appsend.screen.discuss.api.TopicEntry
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface DiscussInteractor {

    fun listTopics(offset: Int? = null): Observable<List<TopicEntry>>

}

class DiscussInteractorImpl(
    private val schedulers: SchedulersFactory
) : DiscussInteractor {

    override fun listTopics(offset: Int?): Observable<List<TopicEntry>> {
        return Single
            .create<List<TopicEntry>> { emitter ->
                val list = listOf(
                    TopicEntry(
                        topicId = 1,
                        type = 0,
                        icon = "",
                        title = "",
                        description = "",
                        packageName = null,
                        readMsgId = null,
                        lastMsg = MessageEntry(
                            userId = 0,
                            userIcon = UserIcon(
                                icon = "",
                                label = mapOf(Pair("en", "Superuser")),
                                color = "#ffddee"
                            ),
                            topicId = 1,
                            msgId = 1,
                            prevMsgId = 0,
                            time = System.currentTimeMillis() / 1000,
                            type = 0,
                            text = "Lorem ipsum dolor",
                            attachment = null,
                            incoming = true,
                        ),
                    )
                )
                emitter.onSuccess(list)
            }
            .toObservable()
            .subscribeOn(schedulers.io())
    }

}