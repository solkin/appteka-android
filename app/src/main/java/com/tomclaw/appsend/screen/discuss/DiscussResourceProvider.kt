package com.tomclaw.appsend.screen.discuss

import android.content.res.Resources
import com.tomclaw.appsend.R

interface DiscussResourceProvider {

    fun commonQuestionsTopicTitle(): String

}

class DiscussResourceProviderImpl(val resources: Resources) : DiscussResourceProvider {

    override fun commonQuestionsTopicTitle(): String {
        return resources.getString(R.string.topic_common_qna)
    }

}