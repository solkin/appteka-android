package com.tomclaw.appsend.screen.chat

import android.content.res.Resources
import com.tomclaw.appsend.R

interface ChatResourceProvider {

    fun unsupportedMessageText(): String

    fun replyFormText(msg: String): String

}

class ChatResourceProviderImpl(val resources: Resources) : ChatResourceProvider {

    override fun unsupportedMessageText(): String {
        return resources.getString(R.string.unsupported_message)
    }

    override fun replyFormText(msg: String): String {
        return resources.getString(R.string.reply_form, msg)
    }

}