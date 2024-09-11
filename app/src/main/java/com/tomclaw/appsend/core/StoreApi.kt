package com.tomclaw.appsend.core

import com.tomclaw.appsend.analytics.api.SubmitEventsRequest
import com.tomclaw.appsend.analytics.api.SubmitEventsResponse
import com.tomclaw.appsend.categories.CategoriesResponse
import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.events.EventsResponse
import com.tomclaw.appsend.screen.auth.request_code.api.RequestCodeResponse
import com.tomclaw.appsend.screen.auth.verify_code.api.VerifyCodeResponse
import com.tomclaw.appsend.screen.chat.api.HistoryResponse
import com.tomclaw.appsend.screen.chat.api.MsgTranslateResponse
import com.tomclaw.appsend.screen.chat.api.ReadTopicResponse
import com.tomclaw.appsend.screen.chat.api.ReportMessageResponse
import com.tomclaw.appsend.screen.chat.api.SendMessageResponse
import com.tomclaw.appsend.screen.chat.api.TopicInfoResponse
import com.tomclaw.appsend.screen.details.api.CreateTopicResponse
import com.tomclaw.appsend.screen.details.api.DeletionResponse
import com.tomclaw.appsend.screen.details.api.Details
import com.tomclaw.appsend.screen.details.api.MarkFavoriteResponse
import com.tomclaw.appsend.screen.details.api.ModerationDecisionResponse
import com.tomclaw.appsend.screen.details.api.TranslationResponse
import com.tomclaw.appsend.screen.favorite.api.FavoriteResponse
import com.tomclaw.appsend.screen.home.api.StartupResponse
import com.tomclaw.appsend.screen.installed.api.CheckUpdatesRequest
import com.tomclaw.appsend.screen.installed.api.CheckUpdatesResponse
import com.tomclaw.appsend.screen.moderation.api.ModerationResponse
import com.tomclaw.appsend.screen.profile.api.EliminateUserResponse
import com.tomclaw.appsend.screen.profile.api.ProfileResponse
import com.tomclaw.appsend.screen.profile.api.SetUserNameResponse
import com.tomclaw.appsend.screen.profile.api.UserAppsResponse
import com.tomclaw.appsend.screen.rate.api.SubmitReviewResponse
import com.tomclaw.appsend.screen.ratings.api.DeleteRatingResponse
import com.tomclaw.appsend.screen.ratings.api.RatingsResponse
import com.tomclaw.appsend.screen.reviews.api.ReviewsResponse
import com.tomclaw.appsend.screen.store.api.AppsListResponse
import com.tomclaw.appsend.screen.topics.api.PinTopicResponse
import com.tomclaw.appsend.screen.topics.api.TopicsResponse
import com.tomclaw.appsend.screen.upload.api.CheckExistResponse
import com.tomclaw.appsend.upload.SetMetaResponse
import com.tomclaw.appsend.user.api.UserBrief
import io.reactivex.rxjava3.core.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface StoreApi {

    @GET("1/app/top/list")
    fun getTopList(
        @Query("app_id") appId: String?,
        @Query("locale") locale: String
    ): Single<StoreResponse<AppsListResponse>>

    @GET("1/app/category/list")
    fun getTopListByCategory(
        @Query("app_id") appId: String?,
        @Query("category_id") categoryId: Int,
        @Query("locale") locale: String
    ): Single<StoreResponse<AppsListResponse>>

    @GET("1/app/moderation/list")
    fun getModerationList(
        @Query("app_id") appId: String?,
        @Query("locale") locale: String
    ): Single<StoreResponse<ModerationResponse>>

    @GET("1/chat/topics")
    fun getTopicsList(
        @Query("offset") offset: Int
    ): Single<StoreResponse<TopicsResponse>>

    @GET("1/chat/topic")
    fun getTopicInfo(
        @Query("topic_id") topicId: Int
    ): Single<StoreResponse<TopicInfoResponse>>

    @FormUrlEncoded
    @POST("1/chat/topic/pin")
    fun pinTopic(
        @Field("topic_id") topicId: Int
    ): Single<StoreResponse<PinTopicResponse>>

    @GET("1/chat/history")
    fun getChatHistory(
        @Query("topic_id") topicId: Int,
        @Query("from") from: Int,
        @Query("till") till: Int
    ): Single<StoreResponse<HistoryResponse>>

    @GET("2/chat/fetch")
    fun getEvents(
        @Query("time") time: Long,
        @Query("nodelay") noDelay: Boolean
    ): Single<StoreResponse<EventsResponse>>

    @GET("1/user/brief")
    fun getUserBrief(
        @Query("user_id") userId: Int?
    ): Single<StoreResponse<UserBrief>>

    @FormUrlEncoded
    @POST("1/chat/push")
    fun sendMessage(
        @Field("topic_id") topicId: Int,
        @Field("text") text: String?,
        @Field("attachment") attachment: String?,
        @Field("cookie") cookie: String
    ): Single<StoreResponse<SendMessageResponse>>

    @FormUrlEncoded
    @POST("1/chat/report")
    fun reportMessage(
        @Field("msg_id") msgId: Int
    ): Single<StoreResponse<ReportMessageResponse>>

    @GET("1/chat/translate")
    fun translateMessage(
        @Query("msg_id") msgId: Int,
        @Query("locale") locale: String,
    ): Single<StoreResponse<MsgTranslateResponse>>

    @FormUrlEncoded
    @POST("1/chat/topic/read")
    fun readTopic(
        @Field("topic_id") topicId: Int,
        @Field("msg_id") msgId: Int
    ): Single<StoreResponse<ReadTopicResponse>>

    @GET("1/categories")
    fun getCategories(): Single<StoreResponse<CategoriesResponse>>

    @GET("1/app/info")
    fun getInfo(
        @Query("app_id") appId: String?,
        @Query("package") packageName: String?
    ): Single<StoreResponse<Details>>

    @GET("1/app/info/translate")
    fun getInfoTranslation(
        @Query("app_id") appId: String,
        @Query("locale") locale: String
    ): Single<StoreResponse<TranslationResponse>>

    @FormUrlEncoded
    @POST("1/app/rate")
    fun submitReview(
        @Field("app_id") appId: String,
        @Field("score") score: Int,
        @Field("text") text: String?
    ): Single<StoreResponse<SubmitReviewResponse>>

    @POST("1/app/moderation/submit")
    fun sendModerationDecision(
        @Query("app_id") appId: String,
        @Query("decision") decision: Int
    ): Single<StoreResponse<ModerationDecisionResponse>>

    @DELETE("1/app/delete")
    fun deleteApplication(
        @Query("app_id") appId: String
    ): Single<StoreResponse<DeletionResponse>>

    @POST("1/chat/topic/create")
    fun createTopic(
        @Query("package") packageName: String
    ): Single<StoreResponse<CreateTopicResponse>>

    @GET("1/app/check_exist")
    fun checkExist(
        @Query("sha1") sha1: String,
        @Query("package") packageName: String,
        @Query("locale") locale: String
    ): Single<StoreResponse<CheckExistResponse>>

    @FormUrlEncoded
    @POST("1/app/meta")
    fun setMeta(
        @Field("app_id") appId: String,
        @Field("category") category: Int,
        @Field("description") description: String,
        @Field("whats_new") whatsNew: String,
        @Field("exclusive") exclusive: Boolean,
        @Field("source_url") sourceUrl: String?,
        @Field("scr_ids") scrIds: List<String>?,
        @Field("private") private: Boolean,
    ): Single<StoreResponse<SetMetaResponse>>

    @POST("1/app/favorite/mark")
    fun markFavorite(
        @Query("app_id") appId: String,
        @Query("is_favorite") isFavorite: Boolean
    ): Single<StoreResponse<MarkFavoriteResponse>>

    @GET("1/app/favorite/list")
    fun getFavoriteList(
        @Query("user_id") userId: Int,
        @Query("app_id") appId: String?,
        @Query("locale") locale: String
    ): Single<StoreResponse<FavoriteResponse>>

    @GET("1/auth/request")
    fun requestCode(
        @Query("email") email: String,
    ): Single<StoreResponse<RequestCodeResponse>>

    @GET("1/auth/verify")
    fun verifyCode(
        @Query("request_id") requestId: String,
        @Query("code") code: String,
        @Query("name") name: String?,
        @Query("guid") guid: String?,
    ): Single<StoreResponse<VerifyCodeResponse>>

    @GET("2/user/profile")
    fun getProfile(
        @Query("user_id") userId: Int?,
    ): Single<StoreResponse<ProfileResponse>>

    @GET("1/user/app/list")
    fun getUserApps(
        @Query("user_id") userId: Int?,
        @Query("app_id") appId: String?,
    ): Single<StoreResponse<UserAppsResponse>>

    @GET("1/user/reviews")
    fun getUserReviews(
        @Query("user_id") userId: Int,
        @Query("rate_id") rateId: Int?,
        @Query("locale") locale: String,
    ): Single<StoreResponse<ReviewsResponse>>

    @DELETE("1/user/eliminate")
    fun eliminateUser(
        @Query("user_id") userId: Int
    ): Single<StoreResponse<EliminateUserResponse>>

    @GET("1/startup")
    fun getStartup(): Single<StoreResponse<StartupResponse>>

    @GET("1/app/rating")
    fun getRatings(
        @Query("app_id") appId: String,
        @Query("rate_id") rateId: Int?,
        @Query("count") count: Int?
    ): Single<StoreResponse<RatingsResponse>>

    @DELETE("1/app/rate/delete")
    fun deleteRating(
        @Query("rate_id") rateId: Int
    ): Single<StoreResponse<DeleteRatingResponse>>

    @POST("1/user/set_name")
    fun setUserName(
        @Query("name") name: String,
    ): Single<StoreResponse<SetUserNameResponse>>

    @Headers("Content-Type: application/json")
    @POST("1/events/submit")
    fun submitEvents(
        @Body body: SubmitEventsRequest,
    ): Single<StoreResponse<SubmitEventsResponse>>

    @POST("1/app/updates")
    fun checkUpdates(
        @Body request: CheckUpdatesRequest
    ): Single<StoreResponse<CheckUpdatesResponse>>

}
