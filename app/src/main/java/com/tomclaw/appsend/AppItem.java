package com.tomclaw.appsend;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import jp.shts.android.library.TriangleLabelView;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppItem extends AbstractAppItem {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

    private View itemView;
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersion;
    private TextView appUpdateTime;
    private TextView appSize;
    private TriangleLabelView badgeNew;

    private static AppIconGlideLoader loader;

    public AppItem(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        appName = (TextView) itemView.findViewById(R.id.app_name);
        appVersion = (TextView) itemView.findViewById(R.id.app_version);
        appUpdateTime = (TextView) itemView.findViewById(R.id.app_update_time);
        appSize = (TextView) itemView.findViewById(R.id.app_size);
        badgeNew = (TriangleLabelView) itemView.findViewById(R.id.badge_new);
    }

    public void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener) {
        if (listener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(info);
                }
            });
        }

        if (loader == null) {
            loader = new AppIconGlideLoader(context.getPackageManager());
        }

        Glide.with(context)
                .using(loader)
                .load(info)
                .into(appIcon);

        appName.setText(info.getLabel());
        if (TextUtils.isEmpty(info.getInstalledVersion())) {
            appVersion.setText(info.getVersion());
        } else {
            appVersion.setText(itemView.getResources().getString(
                    R.string.version_update, info.getInstalledVersion(), info.getVersion()));
        }
        if (info.getLastUpdateTime() > 0) {
            appUpdateTime.setVisibility(View.VISIBLE);
            appUpdateTime.setText(simpleDateFormat.format(info.getLastUpdateTime()));
        } else {
            appUpdateTime.setVisibility(View.GONE);
        }
        appSize.setText(FileHelper.formatBytes(context.getResources(), info.getSize()));

        long appInstallDelay = System.currentTimeMillis() - info.getFirstInstallTime();
        boolean isNewApp = appInstallDelay > 0 && appInstallDelay < TimeUnit.DAYS.toMillis(1);
        badgeNew.setVisibility(isNewApp ? View.VISIBLE : View.GONE);
    }
}
