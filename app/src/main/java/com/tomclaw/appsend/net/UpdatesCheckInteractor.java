package com.tomclaw.appsend.net;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.core.StoreServiceHolder_;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.util.Listeners;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

@EBean(scope = EBean.Scope.Singleton)
public class UpdatesCheckInteractor {

    @App
    Appteka app;

    @Bean
    Session session;

    private Map<String, AppEntry> updates = Collections.emptyMap();

    private Listeners<Map<String, AppEntry>> listeners = new Listeners<>();

    public void checkUpdates() {
        String guid = session.getUserData().getGuid();
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

        CheckUpdatesRequest request = new CheckUpdatesRequest(guid, locale, apps);
        Call<ApiResponse<CheckUpdatesResponse>> call = StoreServiceHolder_.getInstance_(null).getService().checkUpdates(request);
        call.enqueue(new Callback<ApiResponse<CheckUpdatesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<CheckUpdatesResponse>> call, @NonNull retrofit2.Response<ApiResponse<CheckUpdatesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null && response.body().getResult().getEntries() != null) {
                    Map<String, AppEntry> updatesNew = new HashMap<>();
                    for (AppEntry entry : response.body().getResult().getEntries()) {
                        updatesNew.put(entry.getPackageName(), entry);
                    }
                    updates = updatesNew;
                    listeners.notifyListeners(updates);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<CheckUpdatesResponse>> call, @NonNull Throwable t) {
                listeners.notifyListeners(t);
            }
        });
    }

    public Listeners<Map<String, AppEntry>> getListeners() {
        return listeners;
    }

    public Map<String, AppEntry> getUpdates() {
        return Collections.unmodifiableMap(updates);
    }
}
