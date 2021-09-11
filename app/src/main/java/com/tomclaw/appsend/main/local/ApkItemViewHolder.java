package com.tomclaw.appsend.main.local;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.item.ApkItem;
import com.tomclaw.appsend.util.PackageIconLoader;
import com.tomclaw.appsend.util.FileHelper;

import java.util.concurrent.TimeUnit;

import static com.tomclaw.appsend.util.TimeHelper.timeHelper;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.centerCrop;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.withPlaceholder;
import static com.tomclaw.imageloader.util.ImageViewsKt.fetch;

public class ApkItemViewHolder extends FileViewHolder<ApkItem> {

    private View itemView;
    private ImageView appIcon;
    private TextView appName;
    private TextView appVersion;
    private TextView apkCreateTime;
    private TextView appSize;
    private View badgeNew;
    private TextView apkLocation;

    public ApkItemViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appIcon = itemView.findViewById(R.id.app_icon);
        appName = itemView.findViewById(R.id.app_name);
        appVersion = itemView.findViewById(R.id.app_version);
        apkCreateTime = itemView.findViewById(R.id.apk_create_time);
        appSize = itemView.findViewById(R.id.app_size);
        badgeNew = itemView.findViewById(R.id.badge_new);
        apkLocation = itemView.findViewById(R.id.apk_location);
    }

    @Override
    public void bind(final ApkItem item, boolean isLast, final FilesListener<ApkItem> listener) {
        Context context = itemView.getContext();
        if (listener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onClick(item);
                }
            });
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
        if (item.getCreateTime() > 0) {
            apkCreateTime.setVisibility(View.VISIBLE);
            apkCreateTime.setText(timeHelper().getFormattedDate(item.getCreateTime()));
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
