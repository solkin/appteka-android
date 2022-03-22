package com.tomclaw.appsend.main.store;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.net.Session;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import retrofit2.Call;

@EFragment(R.layout.store_fragment)
public class StoreFragment extends BaseStoreFragment {

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @Override
    public Call<ApiResponse<ListResponse>> createCall(String appId, int offset) {
        String locale = getLocaleLanguage();
        return serviceHolder.getService().listTopFiles(session.getUserHolder().getUserData().getGuid(), appId, locale);
    }

}
