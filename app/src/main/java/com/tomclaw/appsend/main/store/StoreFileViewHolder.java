package com.tomclaw.appsend.main.store;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.tomclaw.appsend.categories.CategoryConverterKt.DEFAULT_LOCALE;
import static com.tomclaw.appsend.main.item.StoreItem.FILE_STATUS_MODERATION;
import static com.tomclaw.appsend.main.item.StoreItem.FILE_STATUS_PRIVATE;
import static com.tomclaw.appsend.main.item.StoreItem.FILE_STATUS_UNLINKED;
import static com.tomclaw.appsend.main.item.StoreItem.NOT_INSTALLED;
import static com.tomclaw.appsend.main.ratings.RatingsListener.STATE_FAILED;
import static com.tomclaw.appsend.main.ratings.RatingsListener.STATE_LOADED;
import static com.tomclaw.appsend.main.ratings.RatingsListener.STATE_LOADING;
import static com.tomclaw.appsend.util.DrawablesKt.svgToDrawable;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.centerCrop;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.withPlaceholder;
import static com.tomclaw.imageloader.util.ImageViewsKt.fetch;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.categories.Category;
import com.tomclaw.appsend.categories.CategoryConverter;
import com.tomclaw.appsend.categories.CategoryConverterImpl;
import com.tomclaw.appsend.main.adapter.files.FileViewHolder;
import com.tomclaw.appsend.main.adapter.files.FilesListener;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.LocaleHelper;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class StoreFileViewHolder extends FileViewHolder<StoreItem> {

    private final View itemView;
    private final View appCard;
    private final ImageView appIcon;
    private final TextView appName;
    private final TextView appVersion;
    private final TextView appCategoryTitle;
    private final ImageView appCategoryIcon;
    private final TextView appSize;
    private final TextView appRating;
    private final View ratingIcon;
    private final TextView appDownloads;
    private final View downloadsIcon;
    private final View openSourceIcon;
    private final View appBadge;
    private final TextView appBadgeText;
    private final View badgeNew;
    private final View viewProgress;
    private final View errorView;
    private final View buttonRetry;

    public StoreFileViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        appCard = itemView.findViewById(R.id.app_card);
        appIcon = itemView.findViewById(R.id.app_icon);
        appName = itemView.findViewById(R.id.app_name);
        appVersion = itemView.findViewById(R.id.app_version);
        appCategoryTitle = itemView.findViewById(R.id.app_category);
        appCategoryIcon = itemView.findViewById(R.id.app_category_icon);
        appSize = itemView.findViewById(R.id.app_size);
        appRating = itemView.findViewById(R.id.app_rating);
        ratingIcon = itemView.findViewById(R.id.rating_icon);
        appDownloads = itemView.findViewById(R.id.app_downloads);
        downloadsIcon = itemView.findViewById(R.id.downloads_icon);
        openSourceIcon = itemView.findViewById(R.id.open_source);
        appBadge = itemView.findViewById(R.id.app_badge);
        appBadgeText = itemView.findViewById(R.id.app_badge_text);
        badgeNew = itemView.findViewById(R.id.badge_new);
        viewProgress = itemView.findViewById(R.id.item_progress);
        errorView = itemView.findViewById(R.id.error_view);
        buttonRetry = itemView.findViewById(R.id.button_retry);
    }

    @Override
    public void bind(final StoreItem item, boolean isLast, final FilesListener<StoreItem> listener) {
        appCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(item);
            }
        });

        fetch(appIcon, item.getIcon(), imageViewHandlers -> {
            centerCrop(imageViewHandlers);
            withPlaceholder(imageViewHandlers, R.drawable.app_placeholder);
            imageViewHandlers.setPlaceholder(imageViewViewHolder -> {
                imageViewViewHolder.get().setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViewViewHolder.get().setImageResource(R.drawable.app_placeholder);
                return null;
            });
            return null;
        });

        appName.setText(LocaleHelper.getLocalizedLabel(item));
        appVersion.setText(item.getVersion());

        Category category = item.getCategory();
        if (category != null) {
            appCategoryIcon.setImageDrawable(svgToDrawable(category.getIcon(), itemView.getResources()));
            String title = category.getName().get(Locale.getDefault().getLanguage());
            if (TextUtils.isEmpty(title)) {
                title = category.getName().get(DEFAULT_LOCALE);
            }
            appCategoryTitle.setText(title);
        } else {
            appCategoryIcon.setImageDrawable(null);
            appCategoryTitle.setText(R.string.category_not_set);
        }

        appSize.setText(FileHelper.formatBytes(itemView.getResources(), item.getSize()));
        boolean isInstalled = (item.getInstalledVersionCode() != NOT_INSTALLED);
        boolean isMayBeUpdated = (item.getVersionCode() > item.getInstalledVersionCode());
        boolean isShowDownloads = item.getDownloads() > 0;
        boolean isOpenSource = !TextUtils.isEmpty(item.getSourceURL());
        boolean hasRating = item.getRating() > 0;
        if (isShowDownloads) {
            appDownloads.setText(String.valueOf(item.getDownloads()));
        }
        int badgeTextRes = 0;
        if (isInstalled && isMayBeUpdated) {
            badgeTextRes = R.string.store_app_update;
        } else if (isInstalled) {
            badgeTextRes = R.string.store_app_installed;
        }
        switch (item.getFileStatus()) {
            case FILE_STATUS_UNLINKED:
                badgeTextRes = R.string.status_unlinked;
                appCard.setOnClickListener(null);
                appCard.setClickable(false);
                break;
            case FILE_STATUS_PRIVATE:
                badgeTextRes = R.string.status_private;
                break;
            case FILE_STATUS_MODERATION:
                badgeTextRes = R.string.status_on_moderation;
                break;
        }
        if (hasRating) {
            appRating.setText(String.valueOf(item.getRating()));
        }
        appRating.setVisibility(hasRating ? VISIBLE : GONE);
        ratingIcon.setVisibility(hasRating ? VISIBLE : GONE);
        appDownloads.setVisibility(isShowDownloads ? VISIBLE : GONE);
        downloadsIcon.setVisibility(isShowDownloads ? VISIBLE : GONE);
        openSourceIcon.setVisibility(isOpenSource ? VISIBLE : GONE);
        if (badgeTextRes > 0) {
            appBadgeText.setText(badgeTextRes);
            appBadge.setVisibility(VISIBLE);
        } else {
            appBadge.setVisibility(GONE);
        }


        long appInstallDelay = System.currentTimeMillis() - SECONDS.toMillis(item.getTime());
        boolean isNewApp = appInstallDelay > 0 && appInstallDelay < TimeUnit.DAYS.toMillis(1);
        badgeNew.setVisibility(isNewApp ? VISIBLE : GONE);
        boolean isProgress = false;
        boolean isError = false;
        if (isLast) {
            int result = listener.onNextPage();
            switch (result) {
                case STATE_LOADING:
                    isProgress = true;
                    isError = false;
                    break;
                case STATE_FAILED:
                    isProgress = false;
                    isError = true;
                    break;
                case STATE_LOADED:
                    isProgress = false;
                    isError = false;
                    break;
            }
            viewProgress.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }
        viewProgress.setVisibility(isProgress ? View.VISIBLE : View.GONE);
        errorView.setVisibility(isError ? View.VISIBLE : View.GONE);
        if (isError) {
            buttonRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRetry();
                }
            });
        } else {
            buttonRetry.setOnClickListener(null);
        }
    }

}
