package com.tomclaw.appsend.core

import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.events.EventsResponse
import com.tomclaw.appsend.main.dto.ApiResponse
import com.tomclaw.appsend.main.meta.MetaResponse
import com.tomclaw.appsend.screen.chat.api.HistoryResponse
import com.tomclaw.appsend.screen.chat.api.ReportMessageResponse
import com.tomclaw.appsend.screen.chat.api.SendMessageResponse
import com.tomclaw.appsend.screen.chat.api.TopicInfoResponse
import com.tomclaw.appsend.screen.moderation.api.ModerationResponse
import com.tomclaw.appsend.screen.topics.api.TopicsResponse
import com.tomclaw.appsend.user.api.UserDataResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.*

interface StoreApi {

    @GET("1/app/moderation/list")
    fun getModerationList(
        @Query("guid") guid: String,
        @Query("app_id") appId: String?,
        @Query("locale") locale: String
    ): Single<StoreResponse<ModerationResponse>>

    @GET("1/chat/topics")
    fun getTopicsList(
        @Query("guid") guid: String,
        @Query("offset") offset: Int
    ): Single<StoreResponse<TopicsResponse>>

    @GET("1/chat/topic")
    fun getTopicInfo(
        @Query("guid") guid: String,
        @Query("topic_id") topicId: Int
    ): Single<StoreResponse<TopicInfoResponse>>

    @GET("1/chat/history")
    fun getChatHistory(
        @Query("guid") guid: String,
        @Query("topic_id") topicId: Int,
        @Query("from") from: Int,
        @Query("till") till: Int
    ): Single<StoreResponse<HistoryResponse>>

    @GET("1/chat/fetch")
    fun getEvents(
        @Query("guid") guid: String,
        @Query("time") time: Long,
        @Query("nodelay") noDelay: Boolean
    ): Single<StoreResponse<EventsResponse>>

    @GET("1/user/profile")
    fun getUserData(
        @Query("guid") guid: String
    ): Single<StoreResponse<UserDataResponse>>

    @FormUrlEncoded
    @POST("1/chat/push")
    fun sendMessage(
        @Field("guid") guid: String,
        @Field("topic_id") topicId: Int,
        @Field("text") text: String?,
        @Field("attachment") attachment: String?,
        @Field("cookie") cookie: String
    ): Single<StoreResponse<SendMessageResponse>>

    @FormUrlEncoded
    @POST("1/chat/report")
    fun reportMessage(
        @Field("guid") guid: String,
        @Field("msg_id") msgId: Int
    ): Single<StoreResponse<ReportMessageResponse>>

}