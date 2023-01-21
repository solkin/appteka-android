package com.tomclaw.appsend.main.local;

import static com.tomclaw.appsend.screen.details.DetailsActivityKt.createDetailsActivityIntent;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.centerCrop;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.withPlaceholder;
import static com.tomclaw.imageloader.util.ImageViewsKt.fetch;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.item.AppItem;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.PackageIconLoader;

import java.util.concurrent.TimeUnit;

public class AppItemViewHolder extends FileViewHolder<AppItem> {

    private final View itemView;
    private final ImageView appIcon;
    private final TextView appName;
    private final TextView appVersion;
    private final TextView appUpdateTime;
    private final TextView appSize;
    private final View badgeNew;
    private final View updateButton;

    public AppItemViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appIcon = itemView.findViewById(R.id.app_icon);
        appName = itemView.findViewById(R.id.app_name);
        appVersion = itemView.findViewById(R.id.app_version);
        appUpdateTime = itemView.findViewById(R.id.app_update_time);
        appSize = itemView.findViewById(R.id.app_size);
        badgeNew = itemView.findViewById(R.id.badge_new);
        updateButton = itemView.findViewById(R.id.button_update);
    }

    @Override
    public void bind(final AppItem item, boolean isLast, final FilesListener<AppItem> listener) {
        Context context = itemView.getContext();
        if (listener != null) {
            itemView.setOnClickListener(v -> listener.onClick(item));
        }

        String uri = PackageIconLoader.getUri(item.getPackageInfo());
        fetch(appIcon, uri, imageViewHandlers -> {
            centerCrop(imageViewHandlers);
            withPlaceholder(imageViewHandlers, R.drawable.app_placeholder);
            imageViewHandlers.setPlaceholder(imageViewViewHolder -> {
                imageViewViewHolder.get().setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViewViewHolder.get().setImageResource(R.drawable.app_placeholder);
                return null;
            });
            return null;
        });

        appName.setText(item.getLabel());
        appVersion.setText(item.getVersion());
        if (item.getLastUpdateTime() > 0) {
            appUpdateTime.setVisibility(View.VISIBLE);
            appUpdateTime.setText(timeHelper().getFormattedDate(item.getLastUpdateTime()));
        } else {
            appUpdateTime.setVisibility(View.GONE);
        }
        appSize.setText(FileHelper.formatBytes(context.getResources(), item.getSize()));

        long appInstallDelay = System.currentTimeMillis() - item.getFirstInstallTime();
        boolean isNewApp = appInstallDelay > 0 && appInstallDelay < TimeUnit.DAYS.toMillis(1);
        badgeNew.setVisibility(isNewApp ? View.VISIBLE : View.GONE);

        updateButton.setVisibility(item.getUpdate() != null ? View.VISIBLE : View.GONE);
        updateButton.setOnClickListener(v -> {
            String appId = item.getUpdate().getAppId();
            String label = item.getUpdate().getLabel();
            Intent intent = createDetailsActivityIntent(
                    context,
                    appId,
                    null,
                    label,
                    false,
                    true
            );
            context.startActivity(intent);
        });
    }

}
