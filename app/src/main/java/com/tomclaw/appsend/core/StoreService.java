package com.tomclaw.appsend.core;

import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.meta.MetaResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by solkin on 23.09.17.
 */
public interface StoreService {

    @GET("info.php")
    Call<StoreInfo> getInfo(@Query("v") String apiVer, @Query("app_id") String appId);

    @GET("meta.php")
    Call<MetaResponse> getMeta(@Query("v") int apiVer,
                               @Query("app_id") String appId,
                               @Query("categories") boolean categories);

    @FormUrlEncoded
    @POST("meta.php")
    Call<MetaResponse> setMeta(@Field("v") int apiVer,
                               @Field("app_id") String appId,
                               @Field("guid") String guid,
                               @Field("category") int category,
                               @Field("exclusive") boolean exclusive,
                               @Field("description") String description);

}
