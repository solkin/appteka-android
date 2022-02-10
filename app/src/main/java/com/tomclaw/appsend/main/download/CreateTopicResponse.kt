package com.tomclaw.appsend.main.download

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.TopicEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreateTopicResponse(
    @SerializedName("topic")
    val topic: TopicEntity,
) : Parcelable
