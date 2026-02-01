package com.tomclaw.appsend.screen.feed.api

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
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
            TYPE_FAVORITE -> FavoritePayload::class.java
            TYPE_UPLOAD -> UploadPayload::class.java
            TYPE_SUBSCRIBE -> SubscribePayload::class.java
            else -> UnsupportedPayload::class.java
        }
        val payload = gson.fromJson(obj["payload"].asJsonObject, payloadType)
        val reacts: List<Reaction>? = obj["reacts"]?.let { reactsElement ->
            val type = object : TypeToken<List<Reaction>>() {}.type
            gson.fromJson(reactsElement.asJsonArray, type)
        }
        val user = gson.fromJson(obj["user"].asJsonObject, UserBrief::class.java)
        val actions = obj["actions"]?.let { actionsArray ->
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson<List<String>>(actionsArray.asJsonArray, type)
        }
        return PostEntity(postId, time, type, payload, reacts, user, actions)
    }

}
