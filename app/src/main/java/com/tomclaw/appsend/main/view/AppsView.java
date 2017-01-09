package com.tomclaw.appsend.main.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ViewFlipper;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.DonateActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.UploadActivity;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.controller.AppsController;
import com.tomclaw.appsend.main.task.ExportApkTask;

import java.util.List;

/**
 * Created by ivsolkin on 08.01.17.
 */
public class AppsView extends MainView implements BillingProcessor.IBillingHandler, AppsController.AppsCallback {

    private ViewFlipper viewFlipper;
    private RecyclerView listView;
    private BaseItemAdapter adapter;
    private BaseItemAdapter.BaseItemClickListener listener;
    private BillingProcessor bp;

    public AppsView(Context context) {
        super(context);

        String licenseKey = context.getString(R.string.license_key);
        bp = new BillingProcessor(context, licenseKey, this);

        viewFlipper = (ViewFlipper) findViewById(R.id.apps_view_switcher);

        findViewById(R.id.button_retry).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        listView = (RecyclerView) findViewById(R.id.apps_list_view);
        listView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        listView.setItemAnimator(itemAnimator);

        listener = new BaseItemAdapter.BaseItemClickListener() {
            @Override
            public void onItemClicked(final BaseItem item) {
                boolean donateItem = item.getType() == BaseItem.DONATE_ITEM;
                if (donateItem) {
                    showDonateDialog();
                } else {
                    final AppItem info = (AppItem) item;
                    checkPermissionsForExtract(info);
                }
            }

            @Override
            public void onActionClicked(BaseItem item, String action) {
            }
        };

        adapter = new BaseItemAdapter(context);
        adapter.setListener(listener);
        listView.setAdapter(adapter);
    }

    @Override
    protected int getLayout() {
        return R.layout.apps_view;
    }

    @Override
    public void activate() {
        if (!AppsController.getInstance().isStarted()) {
            refresh();
        }
    }

    @Override
    public void start() {
        AppsController.getInstance().onAttach(this);
    }

    @Override
    public void stop() {
        AppsController.getInstance().onDetach();
    }

    @Override
    public void destroy() {
        if (bp != null) {
            bp.release();
        }
    }

    @Override
    public void refresh() {
        AppsController.getInstance().reload(getContext());
    }

    private void showDonateDialog() {
        startActivity(new Intent(getContext(), DonateActivity.class));
    }

    private void checkPermissionsForExtract(final AppItem appItem) {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    // Permission granted!
                    showActionDialog(appItem);
                } else {
                    // Permission denied.
                    Snackbar.make(listView, R.string.permission_denied_message, Snackbar.LENGTH_LONG).show();
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

    private void showActionDialog(final AppItem appItem) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.app_actions_titles, R.array.app_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                PackageManager packageManager = getContext().getPackageManager();
                                Intent launchIntent = packageManager.getLaunchIntentForPackage(appItem.getPackageName());
                                if (launchIntent == null) {
                                    Snackbar.make(listView, R.string.non_launchable_package, Snackbar.LENGTH_LONG).show();
                                } else {
                                    startActivity(launchIntent);
                                }
                                break;
                            }
                            case 1: {
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), appItem, ExportApkTask.ACTION_SHARE));
                                break;
                            }
                            case 2: {
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), appItem, ExportApkTask.ACTION_EXTRACT));
                                break;
                            }
                            case 3: {
                                Intent intent = new Intent(getContext(), UploadActivity.class);
                                intent.putExtra(UploadActivity.UPLOAD_ITEM, appItem);
                                startActivity(intent);
                                break;
                            }
                            case 4: {
                                TaskExecutor.getInstance().execute(new ExportApkTask(getContext(), appItem, ExportApkTask.ACTION_BLUETOOTH));
                                break;
                            }
                            case 5: {
                                final String appPackageName = appItem.getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                break;
                            }
                            case 6: {
                                setRefreshOnResume();
                                final Intent intent = new Intent()
                                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .addCategory(Intent.CATEGORY_DEFAULT)
                                        .setData(Uri.parse("package:" + appItem.getPackageName()))
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                break;
                            }
                            case 7: {
                                setRefreshOnResume();
                                Uri packageUri = Uri.parse("package:" + appItem.getPackageName());
                                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
                                startActivity(uninstallIntent);
                                break;
                            }
                        }
                    }
                }).show();
    }

    private void setAppInfoList(List<BaseItem> appItemList) {
        if (bp.loadOwnedPurchasesFromGoogle() &&
                bp.isPurchased(getContext().getString(R.string.chocolate_id))) {
            for (BaseItem item : appItemList) {
                boolean donateItem = (item.getType() == BaseItem.DONATE_ITEM);
                if (donateItem) {
                    appItemList.remove(item);
                    break;
                }
            }
        }
        adapter.setItemsList(appItemList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
    }

    @Override
    public void onBillingInitialized() {
    }

    @Override
    public void onProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    @Override
    public void onLoaded(List<BaseItem> list) {
        setAppInfoList(list);
        viewFlipper.setDisplayedChild(1);
    }

    @Override
    public void onError() {
        viewFlipper.setDisplayedChild(2);
    }
}
