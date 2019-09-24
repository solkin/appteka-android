package com.tomclaw.appsend.main.store;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.profile.list.ListResponse;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import retrofit2.Call;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

@EFragment(R.layout.store_fragment)
public class StoreFragment extends BaseStoreFragment {

    @Bean
    StoreServiceHolder serviceHolder;

    @Override
    public Call<ApiResponse<ListResponse>> createCall(String appId, int offset) {
        String locale = getLocaleLanguage();
        return serviceHolder.getService().listFiles(null, appId, locale);
    }

}
