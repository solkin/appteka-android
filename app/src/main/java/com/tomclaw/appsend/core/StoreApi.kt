package com.tomclaw.appsend.core

import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.screen.moderation.api.ModerationResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface StoreApi {

    @GET("1/app/moderation/list")
    fun getModerationList(
        @Query("guid") guid: String,
        @Query("app_id") appId: String?,
        @Query("locale") locale: String
    ): Single<StoreResponse<ModerationResponse>>

    @GET("1/app/moderation/submit")
    fun setModerationDecision(
        @Query("guid") guid: String,
        @Query("app_id") appId: Int,
        @Query("decision") locale: Int
    ): Single<StoreResponse<ModerationResponse>>

}