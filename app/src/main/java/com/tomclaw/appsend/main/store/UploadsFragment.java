package com.tomclaw.appsend.main.store;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import retrofit2.Call;

@EFragment(R.layout.uploads_fragment)
public class UploadsFragment extends BaseStoreFragment {

    @Bean
    StoreServiceHolder serviceHolder;

    @InstanceState
    Long userId;

    @Override
    public Call<ApiResponse<ListResponse>> createCall(String appId, int offset) {
        if (userId == null) return null;
        String locale = getLocaleLanguage();
        return serviceHolder.getService().listUserFiles(userId, appId, locale);
    }

    public void setUserId(long userId) {
        if (this.userId == null || this.userId != userId) {
            this.userId = userId;
            showProgress();
            loadFiles(true);
        }
    }
}
