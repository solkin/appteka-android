package com.tomclaw.appsend.core;

import com.tomclaw.appsend.net.UpdatesCheckInteractor.CheckUpdatesResponse;
import com.tomclaw.appsend.net.UpdatesCheckInteractor.CheckUpdatesRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApptekaService {

    @POST("1/app/updates")
    Call<Response<CheckUpdatesResponse>> checkUpdates(@Body CheckUpdatesRequest request);

}
