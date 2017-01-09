package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.PackageIconGlideLoader;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.FileHelper;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import jp.shts.android.library.TriangleLabelView;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppItemHolder extends AbstractItemHolder<AppItem> {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

    private View itemView;
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersion;
    private TextView appUpdateTime;
    private TextView appSize;
    private TriangleLabelView badgeNew;

    private static PackageIconGlideLoader loader;

    public AppItemHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        appName = (TextView) itemView.findViewById(R.id.app_name);
        appVersion = (TextView) itemView.findViewById(R.id.app_version);
        appUpdateTime = (TextView) itemView.findViewById(R.id.app_update_time);
        appSize = (TextView) itemView.findViewById(R.id.app_size);
        badgeNew = (TriangleLabelView) itemView.findViewById(R.id.badge_new);
    }

    public void bind(Context context, final AppItem item, final BaseItemAdapter.BaseItemClickListener<AppItem> listener) {
        if (listener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(item);
                }
            });
        }

        if (loader == null) {
            loader = new PackageIconGlideLoader(context.getPackageManager());
        }

        try {
            Glide.with(context)
                    .using(loader)
                    .load(item.getPackageInfo())
                    .into(appIcon);
        } catch (Throwable ignored) {
        }

        appName.setText(item.getLabel());
        appVersion.setText(item.getVersion());
        if (item.getLastUpdateTime() > 0) {
            appUpdateTime.setVisibility(View.VISIBLE);
            appUpdateTime.setText(simpleDateFormat.format(item.getLastUpdateTime()));
        } else {
            appUpdateTime.setVisibility(View.GONE);
        }
        appSize.setText(FileHelper.formatBytes(context.getResources(), item.getSize()));

        long appInstallDelay = System.currentTimeMillis() - item.getFirstInstallTime();
        boolean isNewApp = appInstallDelay > 0 && appInstallDelay < TimeUnit.DAYS.toMillis(1);
        badgeNew.setVisibility(isNewApp ? View.VISIBLE : View.GONE);
    }
}
