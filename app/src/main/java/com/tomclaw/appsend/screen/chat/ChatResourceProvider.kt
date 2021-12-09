package com.tomclaw.appsend.screen.chat

import android.content.res.Resources
import com.tomclaw.appsend.R

interface ChatResourceProvider {

    fun unsupportedMessageText(): String

}

class ChatResourceProviderImpl(val resources: Resources) : ChatResourceProvider {

    override fun unsupportedMessageText(): String {
        return resources.getString(R.string.unsupported_message)
    }

}