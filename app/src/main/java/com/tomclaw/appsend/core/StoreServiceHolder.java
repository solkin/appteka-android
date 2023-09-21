package com.tomclaw.appsend.core;

import static com.tomclaw.appsend.core.Config.HOST_URL;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.di.legacy.LegacyModule;

import org.androidannotations.annotations.EBean;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by solkin on 23.09.17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class StoreServiceHolder {

    private final LegacyInjector injector = new LegacyInjector();

    private StoreService service = null;

    private StoreService lazyInit() {
        if (service != null) return service;
        Appteka.getComponent().legacyComponent(new LegacyModule()).inject(injector);

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
