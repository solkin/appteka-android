package com.tomclaw.appsend.main.store.search;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import android.text.TextUtils;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.store.ListResponse;
import com.tomclaw.appsend.main.store.BaseStoreFragment;
import com.tomclaw.appsend.util.Debouncer;

import retrofit2.Call;

public class SearchFragment extends BaseStoreFragment implements Debouncer.Callback<String> {

    private final Debouncer<String> filterDebouncer = new Debouncer<>(this, 1000);

    StoreServiceHolder serviceHolder;

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
                    showProgress();
                    loadFiles(true);
                }
            }
        });
    }

    private boolean isEmptyQuery() {
        return TextUtils.isEmpty(query);
    }

}
