package com.tomclaw.appsend.core;

import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.meta.MetaResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by solkin on 23.09.17.
 */
public interface StoreService {

    @GET("info.php")
    Call<StoreInfo> getInfo(@Query("v") String apiVer, @Query("app_id") String appId);

    @GET("info.php")
    Call<MetaResponse> getMeta(@Query("v") String apiVer,
                               @Query("app_id") String appId,
                               @Query("categories") boolean categories);

}
