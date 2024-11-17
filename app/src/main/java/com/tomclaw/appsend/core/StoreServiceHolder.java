package com.tomclaw.appsend.core;

import static com.tomclaw.appsend.core.ConfigKt.HOST_URL;

import com.tomclaw.appsend.di.legacy.LegacyInjector;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by solkin on 23.09.17.
 */
public class StoreServiceHolder {

    LegacyInjector injector;

    private StoreService service = null;

    private StoreService lazyInit() {
        if (service != null) return service;

        Retrofit retrofit = new Retrofit.Builder()
                .client(injector.httpClient)
                .baseUrl(HOST_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(StoreService.class);
        return service;
    }

    public StoreService getService() {
        return lazyInit();
    }
}
