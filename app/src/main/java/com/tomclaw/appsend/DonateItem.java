package com.tomclaw.appsend;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;

import java.util.concurrent.TimeUnit;

import jp.shts.android.library.TriangleLabelView;

/**
 * Created by ivsolkin on 06.09.16.
 */
public class DonateItem extends AbstractAppItem {

    private View itemView;
    private TriangleLabelView badgeNew;

    public DonateItem(View itemView) {
        super(itemView);
        this.itemView = itemView;
        badgeNew = (TriangleLabelView) itemView.findViewById(R.id.badge_new);
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
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            long appInstallDelay = System.currentTimeMillis() - packageInfo.lastUpdateTime;
            boolean isNewApp = appInstallDelay > 0 && appInstallDelay < TimeUnit.DAYS.toMillis(7);
            badgeNew.setVisibility(isNewApp ? View.VISIBLE : View.GONE);
        } catch (Throwable ignored) {
        }
    }
}
