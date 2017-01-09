package com.tomclaw.appsend.main.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ViewFlipper;

import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.main.item.BaseItem;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.UploadActivity;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.controller.ApksController;
import com.tomclaw.appsend.main.item.CouchItem;
import com.tomclaw.appsend.main.task.ExportApkTask;
import com.tomclaw.appsend.util.PreferenceHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ivsolkin on 08.01.17.
 */
public class InstallView extends MainView implements ApksController.ApksCallback {

    private static final String RETRY_PERMISSION_ACTION = "retry_permission_action";
    private static final String HIDE_COUCH_ACTION = "hide_couch_action";

    private ViewFlipper viewFlipper;
    private RecyclerView listView;
    private BaseItemAdapter adapter;
    private BaseItemAdapter.BaseItemClickListener listener;

    public InstallView(final Context context) {
        super(context);

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
                boolean apkItem = item.getType() == BaseItem.APK_ITEM;
                if (apkItem) {
                    final ApkItem info = (ApkItem) item;
                    showActionDialog(info);
                }
            }

            @Override
            public void onActionClicked(BaseItem item, String action) {
                if (TextUtils.equals(action, RETRY_PERMISSION_ACTION)) {
                    checkPermissionsForInstall();
                } else if (TextUtils.equals(action, HIDE_COUCH_ACTION)) {
                    PreferenceHelper.setShowInstallCouch(context, false);
                    start();
                }
            }
        };

        adapter = new BaseItemAdapter(context);
        adapter.setListener(listener);
        listView.setAdapter(adapter);
    }

    @Override
    protected int getLayout() {
        return R.layout.install_view;
    }

    @Override
    public void activate() {
        checkPermissionsForInstall();
    }

    @Override
    public void start() {
        ApksController.getInstance().onAttach(this);
    }

    @Override
    public void stop() {
        ApksController.getInstance().onDetach();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void refresh() {
        ApksController.getInstance().reload(getContext());
    }

    private void checkPermissionsForInstall() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    // Permission granted!
                    if (!ApksController.getInstance().isStarted()) {
                        refresh();
                    }
                } else {
                    // Permission denied.
                    Snackbar.make(listView, R.string.permission_denied_message, Snackbar.LENGTH_LONG).show();
                    String couchText = getContext().getString(R.string.write_permission_install);
                    String buttonText = getContext().getString(R.string.retry);
                    BaseItem couchItem = new CouchItem(couchText, new CouchItem.CouchButton(RETRY_PERMISSION_ACTION, buttonText));
                    List<BaseItem> list = Collections.singletonList(couchItem);
                    setAppInfoList(list);
                    viewFlipper.setDisplayedChild(1);
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getContext().getString(R.string.app_name);
                String message = getContext().getString(R.string.write_permission_install);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void showActionDialog(final ApkItem apkItem) {
        ListAdapter menuAdapter = new MenuAdapter(getContext(), R.array.apk_actions_titles, R.array.apk_actions_icons);
        new AlertDialog.Builder(getContext())
                .setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                installApp(apkItem);
                                break;
                            }
                            case 1: {
                                ExportApkTask.shareApk(getContext(), new File(apkItem.getPath()));
                                break;
                            }
                            case 2: {
                                Intent intent = new Intent(getContext(), UploadActivity.class);
                                intent.putExtra(UploadActivity.UPLOAD_ITEM, apkItem);
                                startActivity(intent);
                                break;
                            }
                            case 3: {
                                ExportApkTask.bluetoothApk(getContext(), apkItem);
                                break;
                            }
                            case 4: {
                                final String appPackageName = apkItem.getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                                break;
                            }
                            case 5: {
                                File file = new File(apkItem.getPath());
                                file.delete();
                                refresh();
                                break;
                            }
                        }
                    }
                }).show();
    }

    private void installApp(ApkItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(item.getPath())), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setAppInfoList(List<BaseItem> appItemList) {
        adapter.setItemsList(appItemList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    @Override
    public void onLoaded(List<BaseItem> list) {
        List<BaseItem> appItemList = new ArrayList<>();
        boolean isShowCouch = PreferenceHelper.isShowInstallCouch(getContext());
        if (isShowCouch) {
            int couchTextRes;
            if (list.isEmpty()) {
                couchTextRes = R.string.install_screen_no_apk_found;
            } else {
                couchTextRes = R.string.install_screen_apk_found;
            }
            String couchText = getContext().getString(couchTextRes);
            String buttonText = getContext().getString(R.string.got_it);
            BaseItem couchItem = new CouchItem(couchText, new CouchItem.CouchButton(HIDE_COUCH_ACTION, buttonText));
            appItemList.add(couchItem);
        }
        appItemList.addAll(list);
        setAppInfoList(appItemList);
        viewFlipper.setDisplayedChild(1);
    }

    @Override
    public void onError() {
        viewFlipper.setDisplayedChild(2);
    }
}
