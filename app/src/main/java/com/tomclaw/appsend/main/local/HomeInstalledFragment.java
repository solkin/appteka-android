package com.tomclaw.appsend.main.local;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.screen.permissions.PermissionsActivityKt.createPermissionsActivityIntent;
import static com.tomclaw.appsend.screen.upload.UploadActivityKt.createUploadActivityIntent;
import static com.tomclaw.appsend.util.FileHelper.getExternalDirectory;
import static com.tomclaw.appsend.util.IntentHelper.bluetoothApk;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;
import static com.tomclaw.appsend.util.IntentHelper.shareApk;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.Html;
import android.widget.ListAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;
import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.PleaseWaitTask;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.upload.UploadApk;
import com.tomclaw.appsend.upload.UploadPackage;
import com.tomclaw.appsend.util.FileHelper;

import org.androidannotations.annotations.EFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

@EFragment(R.layout.local_apps_fragment)
public class HomeInstalledFragment extends InstalledFragment {

    private void checkExtractPermissions(final AppItem item) {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    showActionDialog(item);
                } else {
                    Snackbar.make(recycler, R.string.permission_denied_message, Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getResources().getString(R.string.app_name);
                String message = getResources().getString(R.string.write_permission_extract);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onClick(final AppItem item) {
        checkExtractPermissions(item);
    }

    private void showActionDialog(final AppItem item) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.app_actions_titles, R.array.app_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0: {
                            PackageManager packageManager = getContext().getPackageManager();
                            Intent launchIntent = packageManager.getLaunchIntentForPackage(item.getPackageName());
                            if (launchIntent == null) {
                                Snackbar.make(recycler, R.string.non_launchable_package, Snackbar.LENGTH_LONG).show();
                            } else {
                                startActivity(launchIntent);
                            }
                            trackEvent("click-run-app");
                            break;
                        }
                        case 1: {
                            TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_SHARE));
                            trackEvent("click-share-apk");
                            break;
                        }
                        case 2: {
                            TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_EXTRACT));
                            trackEvent("click-extract-apk");
                            break;
                        }
                        case 3: {
                            UploadPackage pkg = new UploadPackage(item.getPath(), null, item.getPackageName());
                            UploadApk apk = new UploadApk(item.getPath(), item.getVersion(), item.getSize(), item.getPackageInfo());
                            Intent intent = createUploadActivityIntent(getContext(), pkg, apk, null);
                            startActivity(intent);
                            trackEvent("click-upload-apk");
                            break;
                        }
                        case 4: {
                            TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_BLUETOOTH));
                            trackEvent("click-bluetooth-share");
                            break;
                        }
                        case 5: {
                            String packageName = item.getPackageName();
                            openGooglePlay(getContext(), packageName);
                            trackEvent("click-search-google-play");
                            break;
                        }
                        case 6: {
                            String packageName = item.getPackageName();
                            String label = item.getLabel();
                            Intent intent = createDetailsActivityIntent(
                                    getContext(),
                                    null,
                                    packageName,
                                    label,
                                    false,
                                    true
                            );
                            startActivity(intent);
                            trackEvent("click-search-appteka");
                            break;
                        }
                        case 7: {
                            try {
                                PackageInfo packageInfo = item.getPackageInfo();
                                List<String> permissions = Arrays.asList(packageInfo.requestedPermissions);
                                Intent intent = createPermissionsActivityIntent(getContext(), permissions);
                                startActivity(intent);
                            } catch (Throwable ex) {
                                Snackbar.make(recycler, R.string.unable_to_get_permissions, Snackbar.LENGTH_LONG).show();
                            }
                            break;
                        }
                        case 8: {
                            setRefreshOnResume();
                            final Intent intent = new Intent()
                                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .addCategory(Intent.CATEGORY_DEFAULT)
                                    .setData(Uri.parse("package:" + item.getPackageName()))
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            trackEvent("click-app-details");
                            break;
                        }
                        case 9: {
                            setRefreshOnResume();
                            Uri packageUri = Uri.parse("package:" + item.getPackageName());
                            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                            startActivity(uninstallIntent);
                            trackEvent("click-uninstall-app");
                            break;
                        }
                    }
                }).show();
    }

    /**
     * Created by Solkin on 11.12.2014.
     */
    public static class ExportApkTask extends PleaseWaitTask {

        public static final int ACTION_EXTRACT = 0x00;
        public static final int ACTION_SHARE = 0x01;
        public static final int ACTION_BLUETOOTH = 0x02;

        private final AppItem appItem;
        private final int actionType;

        private File destination;

        public ExportApkTask(Context context, AppItem appItem, int actionType) {
            super(context);
            this.appItem = appItem;
            this.actionType = actionType;
        }

        @Override
        public void executeBackground() throws Throwable {
            Context context = getWeakObject();
            if (context != null) {
                File file = new File(appItem.getPath());
                File directory = getExternalDirectory();
                destination = new File(directory, getApkName(appItem));
                if (destination.exists()) {
                    destination.delete();
                }
                byte[] buffer = new byte[200 * 1024];
                InputStream inputStream = new FileInputStream(file);
                OutputStream outputStream = new FileOutputStream(destination);
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
        }

        public static String getApkPrefix(CommonItem item) {
            return FileHelper.escapeFileSymbols(item.getLabel() + "-" + item.getVersion());
        }

        public static String getApkSuffix() {
            return ".apk";
        }

        public static String getIconSuffix() {
            return ".png";
        }

        public static String getApkName(CommonItem item) {
            return getApkPrefix(item) + getApkSuffix();
        }

        public static String getIconName(CommonItem item) {
            return getApkPrefix(item) + getIconSuffix();
        }

        @Override
        public void onSuccessMain() {
            final Context context = getWeakObject();
            if (context != null) {
                switch (actionType) {
                    case ACTION_EXTRACT: {
                        AlertDialog alertDialog = new AlertDialog.Builder(context)
                                .setTitle(R.string.success)
                                .setMessage(Html.fromHtml(context.getString(R.string.app_extract_success, destination.getPath())))
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        shareApk(context, destination);
                                    }
                                }).setNegativeButton(R.string.no, null)
                                .create();
                        alertDialog.show();
                        break;
                    }
                    case ACTION_SHARE: {
                        shareApk(context, destination);
                        break;
                    }
                    case ACTION_BLUETOOTH: {
                        bluetoothApk(context, destination);
                        break;
                    }
                }
            }
        }

        @Override
        public void onFailMain(Throwable ex) {
            Context context = getWeakObject();
            if (context != null) {
                Toast.makeText(context, R.string.app_extract_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
