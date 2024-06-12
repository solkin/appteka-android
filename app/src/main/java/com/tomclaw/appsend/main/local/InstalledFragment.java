package com.tomclaw.appsend.main.local;

import static com.tomclaw.appsend.util.states.StateHolder.stateHolder;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.di.legacy.LegacyModule;
import com.tomclaw.appsend.main.adapter.files.FileViewHolderCreator;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.net.AppEntry;
import com.tomclaw.appsend.net.UpdatesCheckInteractor;
import com.tomclaw.appsend.util.PreferenceHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@EFragment
abstract class InstalledFragment extends CommonItemFragment<AppItem> {

    private static final String KEY_FILES = "files";

    private ArrayList<AppItem> files;

    @Bean
    UpdatesCheckInteractor updatesCheck;

    @Bean
    LegacyInjector injector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String stateKey = savedInstanceState.getString(KEY_FILES);
            if (stateKey != null) {
                AppItemsState itemsState = stateHolder().removeState(stateKey);
                if (itemsState != null) {
                    files = itemsState.getItems();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (files != null) {
            String stateKey = stateHolder().putState(new AppItemsState(files));
            outState.putString(KEY_FILES, stateKey);
        }
    }

    @Override
    protected List<AppItem> getFiles() {
        return files;
    }

    @Override
    protected void setFiles(List<AppItem> files) {
        if (files != null) {
            this.files = new ArrayList<>(files);
        } else {
            this.files = null;
        }
    }

    @Override
    protected FileViewHolderCreator<AppItem> getViewHolderCreator() {
        return new AppItemViewHolderCreator(getContext());
    }

    @Override
    List<AppItem> loadItemsSync() {
        final Locale locale = Locale.getDefault();
        final Context context = getContext();
        if (context == null) {
            return null;
        }
        boolean isShowSystemApps = PreferenceHelper.isShowSystemApps(context);
        PackageManager packageManager = context.getPackageManager();
        ArrayList<AppItem> appItemList = new ArrayList<>();

        Map<String, AppEntry> updates = updatesCheck.checkUpdatesSync();
        List<ApplicationInfo> packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo info : packages) {
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(info.packageName, 0);
                File file = new File(info.publicSourceDir);
                if (file.exists()) {
                    String label = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                    String version = packageInfo.versionName;
                    long firstInstallTime = packageInfo.firstInstallTime;
                    long lastUpdateTime = packageInfo.lastUpdateTime;
                    AppEntry update = updates.get(info.packageName);
                    AppItem appItem = new AppItem(label, info.packageName, version, file.getPath(),
                            file.length(), firstInstallTime, lastUpdateTime, update, packageInfo);
                    boolean isUserApp = ((info.flags & ApplicationInfo.FLAG_SYSTEM) != ApplicationInfo.FLAG_SYSTEM &&
                            (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != ApplicationInfo.FLAG_UPDATED_SYSTEM_APP);
                    if (isUserApp || isShowSystemApps) {
                        appItemList.add(appItem);
                    }
                }
            } catch (Throwable ignored) {
                // Bad package.
            }
        }
        String sortOrder = PreferenceHelper.getSortOrder(context);
        if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_ascending_value))) {
            Collections.sort(appItemList, (lhs, rhs) -> lhs.getLabel().toUpperCase(locale)
                    .compareTo(rhs.getLabel().toUpperCase(locale)));
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_descending_value))) {
            Collections.sort(appItemList, (lhs, rhs) -> rhs.getLabel().toUpperCase(locale)
                    .compareTo(lhs.getLabel().toUpperCase(locale)));
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_app_size_value))) {
            Collections.sort(appItemList, new Comparator<AppItem>() {
                @Override
                public int compare(AppItem lhs, AppItem rhs) {
                    return compareLong(rhs.getSize(), lhs.getSize());
                }
            });
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_install_time_value))) {
            Collections.sort(appItemList, (lhs, rhs) -> compareLong(rhs.getFirstInstallTime(), lhs.getFirstInstallTime()));
        } else if (TextUtils.equals(sortOrder, context.getString(R.string.sort_order_update_time_value))) {
            Collections.sort(appItemList, (lhs, rhs) -> compareLong(rhs.getLastUpdateTime(), lhs.getLastUpdateTime()));
        }
        Collections.sort(appItemList, (lhs, rhs) -> Boolean.compare(rhs.getUpdate() != null, lhs.getUpdate() != null));
        return appItemList;
    }

    @SuppressWarnings("UseCompareMethod")
    private int compareLong(long lhs, long rhs) {
        return lhs < rhs ? -1 : (lhs == rhs ? 0 : 1);
    }

}
