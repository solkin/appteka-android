package com.tomclaw.appsend.core

import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.screen.moderation.api.ModerationResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface StoreApi {

    @GET("1/app/list")
    fun getModerationList(
        @Query("user_id") userId: Long?,
        @Query("app_id") appId: Int?,
        @Query("locale") locale: String
    ): Single<StoreResponse<ModerationResponse>>

}