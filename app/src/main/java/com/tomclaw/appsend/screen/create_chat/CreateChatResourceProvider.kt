package com.tomclaw.appsend.screen.create_chat

import android.content.res.Resources
import com.tomclaw.appsend.R

interface CreateChatResourceProvider {

    fun createTopicError(): String

    fun avatarRequiredError(): String

    fun titleTooShortError(minLength: Int): String

    fun descriptionTooShortError(minLength: Int): String

}

class CreateChatResourceProviderImpl(
    private val resources: Resources,
) : CreateChatResourceProvider {

    override fun createTopicError(): String =
        resources.getString(R.string.error_create_custom_topic)

    override fun avatarRequiredError(): String =
        resources.getString(R.string.create_chat_avatar_required)

    override fun titleTooShortError(minLength: Int): String =
        resources.getQuantityString(R.plurals.create_chat_title_min_length_error, minLength, minLength)

    override fun descriptionTooShortError(minLength: Int): String =
        resources.getQuantityString(R.plurals.create_chat_description_min_length_error, minLength, minLength)

}
