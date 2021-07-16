package com.tomclaw.appsend.net;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.core.Response;
import com.tomclaw.appsend.util.Listeners;
import com.tomclaw.appsend.util.Unobfuscatable;

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
        Call<Response<CheckUpdatesResponse>> call = Appteka.getService().checkUpdates(request);
        call.enqueue(new Callback<Response<CheckUpdatesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<Response<CheckUpdatesResponse>> call, @NonNull retrofit2.Response<Response<CheckUpdatesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null && response.body().getResult().entries != null) {
                    Map<String, AppEntry> updatesNew = new HashMap<>();
                    for (AppEntry entry : response.body().getResult().entries) {
                        updatesNew.put(entry.packageName, entry);
                    }
                    updates = updatesNew;
                    listeners.notifyListeners(updates);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Response<CheckUpdatesResponse>> call, @NonNull Throwable t) {
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

    public static class CheckUpdatesRequest implements Unobfuscatable {

        @SerializedName(value = "guid")
        private String guid;
        @SerializedName(value = "locale")
        private String locale;
        @SerializedName(value = "apps")
        private Map<String, Integer> apps;

        public CheckUpdatesRequest(String guid, String locale, Map<String, Integer> apps) {
            this.guid = guid;
            this.locale = locale;
            this.apps = apps;
        }

    }

    public static class CheckUpdatesResponse implements Unobfuscatable {
        @SerializedName(value = "entries")
        private List<AppEntry> entries;
    }

    public static class AppEntry implements Unobfuscatable {

        @SerializedName(value = "app_id")
        private String appId;
        @SerializedName(value = "size")
        private String size;
        @SerializedName(value = "time")
        private String time;
        @SerializedName(value = "label")
        private String label;
        @SerializedName(value = "package")
        private String packageName;
        @SerializedName(value = "ver_name")
        private String verName;
        @SerializedName(value = "ver_code")
        private String verCode;
        @SerializedName(value = "downloads")
        private String downloads;
        @SerializedName(value = "user_id")
        private String userId;
        @SerializedName(value = "icon")
        private String icon;

        public String getAppId() {
            return appId;
        }

        public String getSize() {
            return size;
        }

        public String getTime() {
            return time;
        }

        public String getLabel() {
            return label;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getVerName() {
            return verName;
        }

        public String getVerCode() {
            return verCode;
        }

        public String getDownloads() {
            return downloads;
        }

        public String getUserId() {
            return userId;
        }

        public String getIcon() {
            return icon;
        }
    }

}
