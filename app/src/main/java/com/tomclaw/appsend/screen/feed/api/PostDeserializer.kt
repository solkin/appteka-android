package com.tomclaw.appsend.screen.feed.api

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.tomclaw.appsend.user.api.UserBrief
import java.lang.reflect.Type

class PostDeserializer(private val gson: Gson) : JsonDeserializer<PostEntity> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): PostEntity {
        val obj = json.asJsonObject
        val postId = obj["id"].asInt
        val time = obj["time"].asLong
        val type = obj["type"].asInt
        val payloadType = when (type) {
            TYPE_TEXT -> TextPayload::class.java
            else -> throw IllegalArgumentException("Invalid payload type")
        }
        val payload = gson.fromJson(obj["payload"].asJsonObject, payloadType)
        val user = gson.fromJson(obj["user"].asJsonObject, UserBrief::class.java)
        return PostEntity(postId, time, type, payload, user)
    }

}
