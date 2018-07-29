package com.tomclaw.appsend.main.local;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.widget.ListAdapter;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.download.DownloadActivity;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.main.permissions.PermissionsActivity_;
import com.tomclaw.appsend.main.permissions.PermissionsList;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.main.upload.UploadActivity;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;

@EFragment(R.layout.local_apps_fragment)
public class HomeInstalledFragment extends InstalledFragment {

    @Override
    public void onClick(final AppItem item) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.app_actions_titles, R.array.app_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                FlurryAgent.logEvent("App menu: run");
                                PackageManager packageManager = getContext().getPackageManager();
                                Intent launchIntent = packageManager.getLaunchIntentForPackage(item.getPackageName());
                                if (launchIntent == null) {
                                    Snackbar.make(recycler, R.string.non_launchable_package, Snackbar.LENGTH_LONG).show();
                                } else {
                                    startActivity(launchIntent);
                                }
                                break;
                            }
                            case 1: {
                                FlurryAgent.logEvent("App menu: share");
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_SHARE));
                                break;
                            }
                            case 2: {
                                FlurryAgent.logEvent("App menu: extract");
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_EXTRACT));
                                break;
                            }
                            case 3: {
                                FlurryAgent.logEvent("App menu: upload");
                                Intent intent = new Intent(getContext(), UploadActivity.class);
                                intent.putExtra(UploadActivity.UPLOAD_ITEM, item);
                                startActivity(intent);
                                break;
                            }
                            case 4: {
                                FlurryAgent.logEvent("App menu: bluetooth");
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), item, ExportApkTask.ACTION_BLUETOOTH));
                                break;
                            }
                            case 5: {
                                FlurryAgent.logEvent("App menu: Google Play");
                                String packageName = item.getPackageName();
                                openGooglePlay(getContext(), packageName);
                                break;
                            }
                            case 6: {
                                FlurryAgent.logEvent("App menu: AppSend Store");
                                Intent intent = new Intent(getContext(), DownloadActivity.class);
                                intent.putExtra(DownloadActivity.STORE_APP_PACKAGE, item.getPackageName());
                                intent.putExtra(DownloadActivity.STORE_APP_LABEL, item.getLabel());
                                startActivity(intent);
                                break;
                            }
                            case 7: {
                                FlurryAgent.logEvent("App menu: permissions");
                                try {
                                    PackageInfo packageInfo = item.getPackageInfo();
                                    List<String> permissions = Arrays.asList(packageInfo.requestedPermissions);
                                    PermissionsActivity_.intent(getContext())
                                            .permissions(new PermissionsList(new ArrayList<>(permissions)))
                                            .start();
                                } catch (Throwable ex) {
                                    Snackbar.make(recycler, R.string.unable_to_get_permissions, Snackbar.LENGTH_LONG).show();
                                }
                                break;
                            }
                            case 8: {
                                FlurryAgent.logEvent("App menu: details");
                                setRefreshOnResume();
                                final Intent intent = new Intent()
                                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .addCategory(Intent.CATEGORY_DEFAULT)
                                        .setData(Uri.parse("package:" + item.getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            }
                            case 9: {
                                FlurryAgent.logEvent("App menu: remove");
                                setRefreshOnResume();
                                Uri packageUri = Uri.parse("package:" + item.getPackageName());
                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                                startActivity(uninstallIntent);
                                break;
                            }
                        }
                    }
                }).show();
    }

}
