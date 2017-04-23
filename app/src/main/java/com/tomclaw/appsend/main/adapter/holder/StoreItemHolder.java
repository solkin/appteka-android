package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.controller.StoreController;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.LocaleHelper;

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import jp.shts.android.library.TriangleLabelView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.tomclaw.appsend.main.item.StoreItem.NOT_INSTALLED;

/**
 * Created by Solkin on 10.12.2014.
 */
public class StoreItemHolder extends AbstractItemHolder<StoreItem> {

    public static final String ACTION_RETRY = "action_retry";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

    private View itemView;
    private View appCard;
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersion;
    private TextView appUpdateTime;
    private TextView appSize;
    private TextView appDownloads;
    private View downloadsIcon;
    private TextView appBadge;
    private TriangleLabelView badgeNew;
    private View viewProgress;
    private View errorView;
    private View buttonRetry;

    public StoreItemHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appCard = itemView.findViewById(R.id.app_card);
        appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
        appName = (TextView) itemView.findViewById(R.id.app_name);
        appVersion = (TextView) itemView.findViewById(R.id.app_version);
        appUpdateTime = (TextView) itemView.findViewById(R.id.app_update_time);
        appSize = (TextView) itemView.findViewById(R.id.app_size);
        appDownloads = (TextView) itemView.findViewById(R.id.app_downloads);
        downloadsIcon = itemView.findViewById(R.id.downloads_icon);
        appBadge = (TextView) itemView.findViewById(R.id.app_badge);
        badgeNew = (TriangleLabelView) itemView.findViewById(R.id.badge_new);
        viewProgress = itemView.findViewById(R.id.item_progress);
        errorView = itemView.findViewById(R.id.error_view);
        buttonRetry = itemView.findViewById(R.id.button_retry);
    }

    @Override
    View getCardView(View itemView) {
        return itemView.findViewById(R.id.app_card);
    }

    public void bind(Context context, final StoreItem item, final boolean isLast, final BaseItemAdapter.BaseItemClickListener<StoreItem> listener) {
        if (listener != null) {
            appCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(item);
                }
            });
            buttonRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onActionClicked(item, ACTION_RETRY);
                }
            });
        }

        Glide.with(context)
                .load(item.getIcon())
                .into(appIcon);

        appName.setText(LocaleHelper.getLocalizedLabel(item));
        appVersion.setText(item.getVersion());
        if (item.getTime() > 0) {
            appUpdateTime.setVisibility(VISIBLE);
            appUpdateTime.setText(simpleDateFormat.format(item.getTime()));
        } else {
            appUpdateTime.setVisibility(GONE);
        }
        appSize.setText(FileHelper.formatBytes(context.getResources(), item.getSize()));
        boolean isInstalled = (item.getInstalledVersionCode() != NOT_INSTALLED);
        boolean isMayBeUpdated = (item.getVersionCode() > item.getInstalledVersionCode());
        boolean isShowDownloads = !isInstalled && (item.getDownloads() > 0);
        if (isShowDownloads) {
            appDownloads.setText(String.valueOf(item.getDownloads()));
        } else if (isInstalled && isMayBeUpdated) {
            appBadge.setText(R.string.store_app_update);
        } else if (isInstalled) {
            appBadge.setText(R.string.store_app_installed);
        }
        appDownloads.setVisibility(isShowDownloads ? VISIBLE : GONE);
        downloadsIcon.setVisibility(isShowDownloads ? VISIBLE : GONE);
        appBadge.setVisibility(isInstalled ? VISIBLE : GONE);

        long appInstallDelay = System.currentTimeMillis() - item.getTime();
        boolean isNewApp = appInstallDelay > 0 && appInstallDelay < TimeUnit.DAYS.toMillis(1);
        badgeNew.setVisibility(isNewApp ? VISIBLE : GONE);

        boolean isAppendError = isLast && StoreController.getInstance().isError() && StoreController.getInstance().isAppend();
        errorView.setVisibility(isAppendError ? VISIBLE : GONE);
        if (isAppendError) {
            viewProgress.setVisibility(GONE);
        } else {
            boolean isLoad = isLast && StoreController.getInstance().load(
                    context, item.getAppId(), item.getFilter());
            viewProgress.setVisibility(isLoad ? VISIBLE : GONE);
        }
    }
}
