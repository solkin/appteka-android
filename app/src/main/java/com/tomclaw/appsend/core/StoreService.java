package com.tomclaw.appsend.core;

import com.tomclaw.appsend.main.dto.AbuseResult;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.store.ListResponse;
import com.tomclaw.appsend.main.unlink.UnlinkResponse;
import com.tomclaw.appsend.main.unpublish.UnpublishResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by solkin on 23.09.17.
 */
public interface StoreService {

    @GET("api/1/app/abuse")
    Call<ApiResponse<AbuseResult>> reportAbuse(
            @Query("app_id") String appId,
            @Query("reason") String reason,
            @Query("email") String email
    );

    @FormUrlEncoded
    @POST("api/1/app/unlink")
    Call<ApiResponse<UnlinkResponse>> unlink(
            @Field("app_id") String fileId,
            @Field("reason") String reason
    );

    @FormUrlEncoded
    @POST("api/1/app/unpublish")
    Call<ApiResponse<UnpublishResponse>> unpublish(
            @Field("app_id") String fileId,
            @Field("reason") String reason
    );

    @GET("api/1/user/app/list")
    Call<ApiResponse<ListResponse>> listUserFiles(
            @Query("user_id") Long userId,
            @Query("app_id") String appId,
            @Query("locale") String locale
    );

    @GET("api/1/app/search")
    Call<ApiResponse<ListResponse>> searchFiles(
            @Query("query") String query,
            @Query("offset") Integer offset,
            @Query("locale") String locale
    );

}
