package com.tomclaw.appsend.main.store;

import com.tomclaw.appsend.BuildConfig;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.profile.list.ListResponse;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import retrofit2.Call;

@EFragment(R.layout.store_fragment)
public class StoreFragment extends BaseStoreFragment {

    @Bean
    StoreServiceHolder serviceHolder;

    @Override
    public Call<ListResponse> createCall(String appId) {
        int build = BuildConfig.VERSION_CODE;
        return serviceHolder.getService().listFiles(1, null, appId, null, build);
    }

}
