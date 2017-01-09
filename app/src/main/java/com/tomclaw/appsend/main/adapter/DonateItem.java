package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.BaseItem;
import com.tomclaw.appsend.R;

import java.util.concurrent.TimeUnit;

import jp.shts.android.library.TriangleLabelView;

/**
 * Created by ivsolkin on 06.09.16.
 */
public class DonateItem extends AbstractItem {

    private View itemView;
    private TriangleLabelView badgeNew;
    private ImageView appIcon;

    public DonateItem(View itemView) {
        super(itemView);
        this.itemView = itemView;
        badgeNew = (TriangleLabelView) itemView.findViewById(R.id.badge_new);
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
    }

    public void bind(Context context, final BaseItem item, final BaseItemAdapter.BaseItemClickListener listener) {
        Glide.with(context)
                .load("")
                .error(R.drawable.chocolate)
                .into(appIcon);
        if(listener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(item);
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
