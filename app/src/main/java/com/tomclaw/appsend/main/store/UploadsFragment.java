package com.tomclaw.appsend.main.store;

import com.tomclaw.appsend.BuildConfig;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.profile.list.ListResponse;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import retrofit2.Call;

@EFragment(R.layout.store_fragment)
public class UploadsFragment extends BaseStoreFragment {

    @Bean
    StoreServiceHolder serviceHolder;

    @InstanceState
    Long userId;

    @Override
    public Call<ListResponse> createCall(String appId) {
        if (userId == null) return null;
        int build = BuildConfig.VERSION_CODE;
        return serviceHolder.getService().listFiles(1, userId, appId, null, build);
    }

    public void setUserId(long userId) {
        if (this.userId == null || this.userId != userId) {
            this.userId = userId;
            showProgress();
            loadFiles(true);
        }
    }
}
