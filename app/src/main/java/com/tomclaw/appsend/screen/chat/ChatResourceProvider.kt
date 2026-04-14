package com.tomclaw.appsend.screen.chat

import android.content.res.Resources
import com.tomclaw.appsend.R

interface ChatResourceProvider {

    fun unsupportedMessageText(): String

    fun commonQuestionsTopicTitle(): String

    fun commonQuestionsTopicDescription(): String

}

class ChatResourceProviderImpl(val resources: Resources) : ChatResourceProvider {

    override fun unsupportedMessageText(): String {
        return resources.getString(R.string.unsupported_message)
    }

    override fun commonQuestionsTopicTitle(): String {
        return resources.getString(R.string.topic_common_qna_title)
    }

    override fun commonQuestionsTopicDescription(): String {
        return resources.getString(R.string.topic_common_qna_description)
    }

}