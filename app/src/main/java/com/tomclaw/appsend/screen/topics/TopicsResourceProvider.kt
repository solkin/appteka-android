package com.tomclaw.appsend.screen.topics

import android.content.res.Resources
import com.tomclaw.appsend.R

interface TopicsResourceProvider {

    fun commonQuestionsTopicTitle(): String

    fun commonQuestionsTopicDescription(): String

    fun attachmentsPlaceholder(count: Int): String

}

class TopicsResourceProviderImpl(val resources: Resources) : TopicsResourceProvider {

    override fun commonQuestionsTopicTitle(): String {
        return resources.getString(R.string.topic_common_qna_title)
    }

    override fun commonQuestionsTopicDescription(): String {
        return resources.getString(R.string.topic_common_qna_description)
    }

    override fun attachmentsPlaceholder(count: Int): String {
        return resources.getQuantityString(R.plurals.topic_last_msg_attachments, count, count)
    }

}