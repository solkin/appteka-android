package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.util.PackageIconGlideLoader;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.util.FileHelper;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import jp.shts.android.library.TriangleLabelView;

/**
 * Created by Solkin on 10.12.2014.
 */
public class ApkItemHolder extends AbstractItemHolder<ApkItem> {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

    private View itemView;
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersion;
    private TextView apkCreateTime;
    private TextView appSize;
    private TriangleLabelView badgeNew;
    private TextView apkLocation;

    private static PackageIconGlideLoader loader;

    public ApkItemHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        appName = (TextView) itemView.findViewById(R.id.app_name);
        appVersion = (TextView) itemView.findViewById(R.id.app_version);
        apkCreateTime = (TextView) itemView.findViewById(R.id.apk_create_time);
        appSize = (TextView) itemView.findViewById(R.id.app_size);
        badgeNew = (TriangleLabelView) itemView.findViewById(R.id.badge_new);
        apkLocation = (TextView) itemView.findViewById(R.id.apk_location);
    }

    public void bind(Context context, final ApkItem item, final boolean isLast, final BaseItemAdapter.BaseItemClickListener<ApkItem> listener) {
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
        Glide.with(context)
                .using(loader)
                .load(item.getPackageInfo())
                .into(appIcon);

        appName.setText(item.getLabel());
        if (TextUtils.isEmpty(item.getInstalledVersion())) {
            appVersion.setText(item.getVersion());
        } else {
            appVersion.setText(itemView.getResources().getString(
                    R.string.version_update, item.getInstalledVersion(), item.getVersion()));
        }
        if (item.getCreateTime() > 0) {
            apkCreateTime.setVisibility(View.VISIBLE);
            apkCreateTime.setText(simpleDateFormat.format(item.getCreateTime()));
        } else {
            apkCreateTime.setVisibility(View.GONE);
        }
        appSize.setText(FileHelper.formatBytes(context.getResources(), item.getSize()));

        long apkCreateDelay = System.currentTimeMillis() - item.getCreateTime();
        boolean isNewApp = apkCreateDelay > 0 && apkCreateDelay < TimeUnit.DAYS.toMillis(1);
        badgeNew.setVisibility(isNewApp ? View.VISIBLE : View.GONE);

        apkLocation.setText(item.getPath());
    }
}
