package com.tomclaw.appsend.core;

import com.tomclaw.appsend.dto.StoreResponse;
import com.tomclaw.appsend.main.auth.AuthResponse;
import com.tomclaw.appsend.main.dto.AbuseResult;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.meta.MetaResponse;
import com.tomclaw.appsend.main.profile.EmpowerResponse;
import com.tomclaw.appsend.main.profile.ProfileResponse;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.main.ratings.RateResponse;
import com.tomclaw.appsend.main.ratings.RatingsResponse;
import com.tomclaw.appsend.main.unlink.UnlinkResponse;
import com.tomclaw.appsend.net.CheckUpdatesRequest;
import com.tomclaw.appsend.net.CheckUpdatesResponse;
import com.tomclaw.appsend.screen.moderation.api.ModerationResponse;

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

    @GET("api/1/app/info")
    Call<ApiResponse<StoreInfo>> getInfo(
            @Query("v") int apiVer,
            @Query("guid") String guid,
            @Query("app_id") String appId,
            @Query("package") String packageName
    );

    @GET("api/1/app/meta")
    Call<ApiResponse<MetaResponse>> getMeta(
            @Query("v") int apiVer,
            @Query("app_id") String appId,
            @Query("categories") boolean categories
    );

    @FormUrlEncoded
    @POST("api/1/app/meta")
    Call<ApiResponse<MetaResponse>> setMeta(
            @Field("v") int apiVer,
            @Field("app_id") String appId,
            @Field("guid") String guid,
            @Field("category") int category,
            @Field("exclusive") int exclusive,
            @Field("description") String description
    );

    @GET("api/1/app/rating")
    Call<ApiResponse<RatingsResponse>> getRatings(
            @Query("v") int apiVer,
            @Query("app_id") String appId,
            @Query("rate_id") int rateId,
            @Query("count") int count
    );

    @FormUrlEncoded
    @POST("api/1/app/rate")
    Call<ApiResponse<RateResponse>> setRating(
            @Field("v") int apiVer,
            @Field("app_id") String appId,
            @Field("guid") String guid,
            @Field("score") int score,
            @Field("text") String text
    );

    @GET("api/1/app/abuse")
    Call<ApiResponse<AbuseResult>> reportAbuse(
            @Query("v") int apiVer,
            @Query("app_id") String appId,
            @Query("reason") String reason,
            @Query("email") String email
    );

    @GET("api/1/user/profile")
    Call<ApiResponse<ProfileResponse>> getProfile(
            @Query("v") int apiVer,
            @Query("guid") String guid,
            @Query("user_id") String userId
    );

    @FormUrlEncoded
    @POST("api/1/user/empower")
    Call<ApiResponse<EmpowerResponse>> empower(
            @Field("v") int apiVer,
            @Field("guid") String guid,
            @Field("role") int role,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("api/1/app/unlink")
    Call<ApiResponse<UnlinkResponse>> unlink(
            @Field("v") int apiVer,
            @Field("guid") String guid,
            @Field("app_id") String fileId,
            @Field("reason") String reason
    );

    @GET("api/1/app/list")
    Call<ApiResponse<ListResponse>> listFiles(
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

    @FormUrlEncoded
    @POST("api/1/user/register")
    Call<ApiResponse<AuthResponse>> register(
            @Field("v") int apiVer,
            @Field("guid") String guid,
            @Field("locale") String locale,
            @Field("email") String email,
            @Field("password") String password,
            @Field("name") String name
    );

    @FormUrlEncoded
    @POST("api/1/user/login")
    Call<ApiResponse<AuthResponse>> login(
            @Field("v") int apiVer,
            @Field("locale") String locale,
            @Field("email") String email,
            @Field("password") String password
    );


    @POST("api/1/app/updates")
    Call<ApiResponse<CheckUpdatesResponse>> checkUpdates(
            @Body CheckUpdatesRequest request
    );

    @POST("api/1/app/moderation/submit")
    Call<StoreResponse<ModerationResponse>> setModerationDecision(
            @Query("guid") String guid,
            @Query("app_id") String appId,
            @Query("decision") Integer decision
    );

}
