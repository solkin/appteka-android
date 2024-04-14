package com.tomclaw.appsend.net;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

@EBean(scope = EBean.Scope.Singleton)
public class UpdatesCheckInteractor {

    @App
    Appteka app;

    @Bean
    StoreServiceHolder serviceHolder;

    public Map<String, AppEntry> checkUpdatesSync() {
        CheckUpdatesRequest request = getCheckUpdatesRequest();
        Call<ApiResponse<CheckUpdatesResponse>> call = serviceHolder.getService().checkUpdates(request);
        try {
            retrofit2.Response<ApiResponse<CheckUpdatesResponse>> response = call.execute();
            if (response.isSuccessful() && response.body() != null && response.body().getResult() != null && response.body().getResult().getEntries() != null) {
                Map<String, AppEntry> updatesNew = new HashMap<>();
                for (AppEntry entry : response.body().getResult().getEntries()) {
                    updatesNew.put(entry.getPackageName(), entry);
                }
                return updatesNew;
            }
        } catch (IOException ignored) {
        }
        return Collections.emptyMap();
    }

    @NonNull
    private CheckUpdatesRequest getCheckUpdatesRequest() {
        String locale = getLocaleLanguage();
        Map<String, Integer> apps = new HashMap<>();

        PackageManager packageManager = app.getPackageManager();

        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : packages) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(info.packageName, 0);
                boolean isUserApp = ((info.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                        (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
                if (isUserApp) {
                    apps.put(info.packageName, packageInfo.versionCode);
                }
            } catch (Throwable ignored) {
            }
        }

        return new CheckUpdatesRequest(locale, apps);
    }

}
