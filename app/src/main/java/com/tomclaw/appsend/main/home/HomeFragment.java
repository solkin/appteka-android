package com.tomclaw.appsend.main.home;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.tomclaw.appsend.util.Debouncer;

public abstract class HomeFragment extends Fragment implements Debouncer.Callback<String> {

    private Debouncer<String> filterDebouncer = new Debouncer<>(this, 500);

    private String query;

    public boolean isFilterable() {
        return false;
    }

    public final void filter(String query) {
        if (!TextUtils.equals(this.query, query)) {
            this.query = query;
            filterDebouncer.call("");
        }
    }

    @Override
    public final void call(String key) {
        runFilter(query);
    }

    public void runFilter(String query) {
    }
}
