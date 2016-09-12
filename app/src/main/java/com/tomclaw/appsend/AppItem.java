package com.tomclaw.appsend;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

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

    public AppItem(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        appName = (TextView) itemView.findViewById(R.id.app_name);
        appVersion = (TextView) itemView.findViewById(R.id.app_version);
        appUpdateTime = (TextView) itemView.findViewById(R.id.app_update_time);
        appSize = (TextView) itemView.findViewById(R.id.app_size);
    }

    public void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener) {
        if(listener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(info);
                }
            });
        }
        appIcon.setImageDrawable(info.getIcon());
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
    }
}
