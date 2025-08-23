package com.tomclaw.appsend.core;

import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.store.ListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by solkin on 23.09.17.
 */
public interface StoreService {

    @GET("api/1/app/search")
    Call<ApiResponse<ListResponse>> searchFiles(
            @Query("query") String query,
            @Query("offset") Integer offset,
            @Query("locale") String locale
    );

}
