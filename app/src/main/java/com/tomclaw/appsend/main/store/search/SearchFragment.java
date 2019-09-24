package com.tomclaw.appsend.main.store.search;

import android.text.TextUtils;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.main.store.BaseStoreFragment;
import com.tomclaw.appsend.util.Debouncer;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import retrofit2.Call;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

@EFragment(R.layout.store_fragment)
public class SearchFragment extends BaseStoreFragment implements Debouncer.Callback<String> {

    private Debouncer<String> filterDebouncer = new Debouncer<>(this, 350);

    @Bean
    StoreServiceHolder serviceHolder;

    @InstanceState
    String query;

    @Override
    public Call<ApiResponse<ListResponse>> createCall(String appId, int offset) {
        if (isEmptyQuery()) {
            return null;
        }
        String locale = getLocaleLanguage();
        return serviceHolder.getService().searchFiles(query, offset, locale);
    }

    public final void filter(String query) {
        if (!TextUtils.equals(this.query, query)) {
            this.query = query;
            filterDebouncer.call("");
        }
    }

    @Override
    public final void call(String key) {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (isEmptyQuery()) {
                    clearFiles();
                } else {
                    loadFiles(true);
                }
            }
        });
    }

    private boolean isEmptyQuery() {
        return TextUtils.isEmpty(query);
    }

}
