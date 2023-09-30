package com.tomclaw.appsend.core;

import com.tomclaw.appsend.main.auth.AuthResponse;
import com.tomclaw.appsend.main.dto.AbuseResult;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.profile.EliminateUserResponse;
import com.tomclaw.appsend.main.profile.EmpowerResponse;
import com.tomclaw.appsend.main.profile.ProfileResponse;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.main.ratings.RatingsResponse;
import com.tomclaw.appsend.main.ratings.VoidResponse;
import com.tomclaw.appsend.main.unlink.UnlinkResponse;
import com.tomclaw.appsend.main.unpublish.UnpublishResponse;
import com.tomclaw.appsend.net.CheckUpdatesRequest;
import com.tomclaw.appsend.net.CheckUpdatesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by solkin on 23.09.17.
 */
public interface StoreService {

    @GET("api/1/app/rating")
    Call<ApiResponse<RatingsResponse>> getRatings(
            @Query("app_id") String appId,
            @Query("rate_id") int rateId,
            @Query("count") int count
    );

    @FormUrlEncoded
    @POST("api/1/app/rate")
    Call<ApiResponse<VoidResponse>> setRating(
            @Field("app_id") String appId,
            @Field("score") int score,
            @Field("text") String text
    );

    @DELETE("api/1/app/rate/delete")
    Call<ApiResponse<VoidResponse>> deleteRating(
            @Query("rate_id") int rateId
    );

    @GET("api/1/app/abuse")
    Call<ApiResponse<AbuseResult>> reportAbuse(
            @Query("app_id") String appId,
            @Query("reason") String reason,
            @Query("email") String email
    );

    @GET("api/1/user/profile")
    Call<ApiResponse<ProfileResponse>> getProfile(
            @Query("user_id") String userId
    );

    @FormUrlEncoded
    @POST("api/1/user/empower")
    Call<ApiResponse<EmpowerResponse>> empower(
            @Field("role") int role,
            @Field("user_id") String userId
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

    @POST("api/1/app/updates")
    Call<ApiResponse<CheckUpdatesResponse>> checkUpdates(
            @Body CheckUpdatesRequest request
    );

    @DELETE("api/1/user/eliminate")
    Call<ApiResponse<EliminateUserResponse>> eliminateUser(
            @Query("user_id") Long userId
    );

}
