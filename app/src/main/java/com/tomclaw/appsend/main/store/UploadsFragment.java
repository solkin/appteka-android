package com.tomclaw.appsend.main.store;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;

import retrofit2.Call;

public class UploadsFragment extends BaseStoreFragment {

    StoreServiceHolder serviceHolder;

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
