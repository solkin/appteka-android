package com.tomclaw.appsend.net;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.AppSend;
import com.tomclaw.appsend.core.Response;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class AppUpdatesChecker {

    private static AppUpdatesChecker instance;

    private AppUpdatesChecker() {
    }

    public static void init() {
        instance = new AppUpdatesChecker();
    }

    public static AppUpdatesChecker stateHolder() {
        if (instance == null) {
            throw new IllegalStateException("AppUpdatesChecker must be initialized first");
        }
        return instance;
    }

    public void checkUpdates() {
        String guid = Session_.getInstance().getUserData().getGuid();
        Map<String, Integer> apps = new HashMap<>();

        PackageManager packageManager = AppSend.app().getPackageManager();

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

        CheckUpdatesRequest request = new CheckUpdatesRequest(guid, apps);
        Call<Response<CheckUpdatesResponse>> call = AppSend.getService().checkUpdates(request);
        call.enqueue(new Callback<Response<CheckUpdatesResponse>>() {
            @Override
            public void onResponse(Call<Response<CheckUpdatesResponse>> call, retrofit2.Response<Response<CheckUpdatesResponse>> response) {
                Logger.log(response.toString());
            }

            @Override
            public void onFailure(Call<Response<CheckUpdatesResponse>> call, Throwable t) {

            }
        });
    }

    public static class CheckUpdatesRequest {

        @SerializedName(value = "guid")
        private String guid;
        @SerializedName(value = "apps")
        private Map<String, Integer> apps;

        public CheckUpdatesRequest(String guid, Map<String, Integer> apps) {
            this.guid = guid;
            this.apps = apps;
        }

    }

    public static class CheckUpdatesResponse {
        private List<AppEntries> entries;
    }

    public static class AppEntries {

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
