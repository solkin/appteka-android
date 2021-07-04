package com.tomclaw.appsend.core;

import com.tomclaw.appsend.net.AppUpdatesChecker.CheckUpdatesResponse;
import com.tomclaw.appsend.net.AppUpdatesChecker.CheckUpdatesRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApptekaService {

    @POST("1/app/updates")
    Call<Response<CheckUpdatesResponse>> checkUpdates(@Body CheckUpdatesRequest request);

}
