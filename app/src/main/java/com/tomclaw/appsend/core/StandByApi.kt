package com.tomclaw.appsend.core

import com.tomclaw.appsend.dto.StoreResponse
import com.tomclaw.appsend.screen.home.api.StatusResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface StandByApi {

    @GET("status.php")
    fun getStatus(
        @Query("locale") locale: String,
        @Query("build") build: Long,
    ): Single<StoreResponse<StatusResponse>>

}