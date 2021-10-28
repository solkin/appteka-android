package com.tomclaw.appsend.main.download;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.material.snackbar.Snackbar;
import com.greysonparrelli.permiso.Permiso;
import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.core.StoreServiceHolder_;
import com.tomclaw.appsend.dto.StoreResponse;
import com.tomclaw.appsend.main.abuse.AbuseActivity_;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.dto.RatingItem;
import com.tomclaw.appsend.main.dto.StoreInfo;
import com.tomclaw.appsend.main.dto.StoreVersion;
import com.tomclaw.appsend.main.home.HomeActivity;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.meta.Category;
import com.tomclaw.appsend.main.meta.Meta;
import com.tomclaw.appsend.main.meta.MetaActivity;
import com.tomclaw.appsend.main.meta.Scores;
import com.tomclaw.appsend.main.permissions.PermissionsActivity_;
import com.tomclaw.appsend.main.permissions.PermissionsList;
import com.tomclaw.appsend.main.profile.ProfileActivity_;
import com.tomclaw.appsend.main.ratings.RateResponse;
import com.tomclaw.appsend.main.ratings.RatingsActivity_;
import com.tomclaw.appsend.main.ratings.UserRating;
import com.tomclaw.appsend.main.unlink.UnlinkActivity_;
import com.tomclaw.appsend.main.unpublish.UnpublishActivity_;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.main.view.PlayView;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.screen.moderation.api.ModerationResponse;
import com.tomclaw.appsend.util.FileHelper;
import com.tomclaw.appsend.util.IntentHelper;
import com.tomclaw.appsend.util.KeyboardHelper;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.PermissionHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.ThemeHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.main.ratings.RatingsHelper.tintRatingIndicator;
import static com.tomclaw.appsend.util.ColorHelper.getAttributedColor;
import static com.tomclaw.appsend.util.FileHelper.getExternalDirectory;
import static com.tomclaw.appsend.util.IntentHelper.formatText;
import static com.tomclaw.appsend.util.IntentHelper.openGooglePlay;
import static com.tomclaw.appsend.util.IntentHelper.shareUrl;
import static com.tomclaw.appsend.util.LocaleHelper.getLocalizedName;
import static com.tomclaw.appsend.util.PermissionHelper.getPermissionSmallInfo;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.centerCrop;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.withPlaceholder;
import static com.tomclaw.imageloader.util.ImageViewsKt.fetch;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by ivsolkin on 14.01.17.
 */
public class DownloadActivity extends PermisoActivity implements DownloadController.DownloadCallback {

    public static final String STORE_APP_ID = "app_id";
    public static final String STORE_APP_PACKAGE = "app_package";
    public static final String STORE_APP_LABEL = "app_label";
    public static final String STORE_FINISH_ONLY = "finish_only";
    public static final String STORE_MODERATION = "moderation";
    private static final String UNLINK_ACTION = "unlink";
    private static final String UNPUBLISH_ACTION = "unpublish";
    private static final String EDIT_META_ACTION = "edit_meta";
    private static final int REQUEST_CODE_UNLINK = 42;

    private static final int REQUEST_UPDATE_META = 4;

    private static final int MAX_PERMISSIONS_COUNT = 3;
    private static final long DEBOUNCE_DELAY = 100;

    private String appId;
    private String appPackage;
    private String appLabel;
    private boolean finishOnly;
    private boolean moderation;

    private ViewFlipper viewFlipper;
    private TextView errorText;
    private ImageView iconView;
    private TextView labelView;
    private TextView packageView;
    private PlayView downloadsView;
    private PlayView sizeView;
    private PlayView minAndroidView;
    private View metaBlockView;
    private TextView descriptionView;
    private MemberImageView descriptionAuthorAvatar;
    private RelativeLayout permissionsBlock;
    private ViewGroup permissionsContainer;
    private View uploaderContainerView;
    private MemberImageView uploaderAvatar;
    private TextView versionView;
    private TextView uploadedTimeView;
    private TextView checksumView;
    private View shadowView;
    private View readMoreButton;
    private View otherVersionsTitle;
    private ViewGroup versionsContainer;
    private ViewFlipper buttonsSwitcher;
    private Button buttonOne;
    private Button buttonFirst;
    private Button buttonSecond;
    private ProgressBar progress;
    private TextView extraAccess;
    private SwipeRefreshLayout swipeRefresh;
    private View rateContainer;
    private View ratingContainer;
    private ViewGroup ratingItemsContainer;
    private TextView ratingScore;
    private TextView ratesCount;
    private RatingBar smallRatingIndicator;
    private ProgressBar rdeFive;
    private ProgressBar rdeFour;
    private ProgressBar rdeThree;
    private ProgressBar rdeTwo;
    private ProgressBar rdeOne;
    private View categoryContainer;
    private SVGImageView categoryIcon;
    private TextView categoryTitle;
    private MemberImageView ratingMemberAvatar;
    private ViewFlipper ratingFlipper;
    private RatingBar userRating;
    private EditText userOpinion;
    private View exclusiveBadge;
    private MenuItem abuseItem;
    private View metaContainer;
    private View editMeta;

    private StoreInfo info;

    private transient long progressUpdateTime = 0;

    private StoreServiceHolder serviceHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        serviceHolder = StoreServiceHolder_.getInstance_(this);

        setContentView(R.layout.download_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        boolean isCreateInstance = savedInstanceState == null;
        if (isCreateInstance) {
            Uri data = getIntent().getData();
            if (data != null && data.getHost() != null) {
                if (data.getHost().equals("appteka.store")) {
                    List<String> path = data.getPathSegments();
                    if (path.size() == 2) {
                        appId = path.get(1);
                    }
                } else if (data.getHost().equals("appsend.store")) {
                    appId = data.getQueryParameter("id");
                    appPackage = data.getQueryParameter("package");
                }
                appLabel = getString(R.string.download);
            } else if (TextUtils.isEmpty(appId)) {
                appId = getIntent().getStringExtra(STORE_APP_ID);
                appPackage = getIntent().getStringExtra(STORE_APP_PACKAGE);
                appLabel = getIntent().getStringExtra(STORE_APP_LABEL);
                finishOnly = getIntent().getBooleanExtra(STORE_FINISH_ONLY, false);
                moderation = getIntent().getBooleanExtra(STORE_MODERATION, false);
            }
        } else {
            appId = savedInstanceState.getString(STORE_APP_ID);
            appPackage = savedInstanceState.getString(STORE_APP_ID);
            appLabel = savedInstanceState.getString(STORE_APP_LABEL);
            finishOnly = savedInstanceState.getBoolean(STORE_FINISH_ONLY, false);
            moderation = savedInstanceState.getBoolean(STORE_MODERATION, false);
        }
        if (TextUtils.isEmpty(appId) && TextUtils.isEmpty(appPackage)) {
            openStore();
            finish();
        }

        setTitle(appLabel);

        viewFlipper = findViewById(R.id.view_flipper);
        errorText = findViewById(R.id.error_text);
        iconView = findViewById(R.id.app_icon);
        labelView = findViewById(R.id.app_label);
        packageView = findViewById(R.id.app_package);
        downloadsView = findViewById(R.id.app_downloads);
        sizeView = findViewById(R.id.app_size);
        minAndroidView = findViewById(R.id.min_android);
        metaBlockView = findViewById(R.id.meta_block);
        descriptionView = findViewById(R.id.description);
        descriptionAuthorAvatar = findViewById(R.id.description_author_avatar);
        permissionsBlock = findViewById(R.id.permissions_block);
        permissionsContainer = findViewById(R.id.permissions_container);
        uploaderContainerView = findViewById(R.id.uploader_container);
        uploaderAvatar = findViewById(R.id.uploader_avatar);
        versionView = findViewById(R.id.app_version);
        uploadedTimeView = findViewById(R.id.uploaded_time);
        checksumView = findViewById(R.id.app_checksum);
        shadowView = findViewById(R.id.read_more_shadow);
        readMoreButton = findViewById(R.id.read_more_button);
        otherVersionsTitle = findViewById(R.id.other_versions_title);
        versionsContainer = findViewById(R.id.app_versions);
        buttonsSwitcher = findViewById(R.id.buttons_switcher);
        buttonOne = findViewById(R.id.button_one);
        buttonFirst = findViewById(R.id.button_first);
        buttonSecond = findViewById(R.id.button_second);
        progress = findViewById(R.id.progress);
        extraAccess = findViewById(R.id.extra_access);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        rateContainer = findViewById(R.id.rate_container);
        ratingContainer = findViewById(R.id.rating_container);
        ratingItemsContainer = findViewById(R.id.rating_items);
        ratingScore = findViewById(R.id.rating_score);
        ratesCount = findViewById(R.id.rates_count);
        ratingMemberAvatar = findViewById(R.id.rating_member_avatar);
        ratingFlipper = findViewById(R.id.rating_flipper);
        userRating = findViewById(R.id.user_rating_view);
        userOpinion = findViewById(R.id.user_opinion);
        exclusiveBadge = findViewById(R.id.exclusive_badge);

        rdeFive = findViewById(R.id.rating_detail_element_five);
        rdeFour = findViewById(R.id.rating_detail_element_four);
        rdeThree = findViewById(R.id.rating_detail_element_three);
        rdeTwo = findViewById(R.id.rating_detail_element_two);
        rdeOne = findViewById(R.id.rating_detail_element_one);

        smallRatingIndicator = findViewById(R.id.small_rating_indicator);
        tintRatingIndicator(this, smallRatingIndicator);

        tintProgress(rdeFive, getAttributedColor(this, R.attr.five_stars));
        tintProgress(rdeFour, getAttributedColor(this, R.attr.four_stars));
        tintProgress(rdeThree, getAttributedColor(this, R.attr.three_stars));
        tintProgress(rdeTwo, getAttributedColor(this, R.attr.two_stars));
        tintProgress(rdeOne, getAttributedColor(this, R.attr.one_stars));

        categoryContainer = findViewById(R.id.category);
        categoryIcon = findViewById(R.id.category_icon);
        categoryTitle = findViewById(R.id.category_title);

        metaContainer = findViewById(R.id.meta_container);
        editMeta = findViewById(R.id.edit_meta);

        swipeRefresh.setOnRefreshListener(this::reloadInfo);
        findViewById(R.id.button_cancel).setOnClickListener(v -> cancelDownload());
        findViewById(R.id.button_retry).setOnClickListener(v -> reloadInfo());
        findViewById(R.id.share_button).setOnClickListener(v -> {
            String text = formatText(getResources(), info.getUrl(),
                    LocaleHelper.getLocalizedLabel(info.getItem()), info.getItem().getSize());
            shareUrl(DownloadActivity.this, text);
            trackEvent("share-app-url");
        });
        findViewById(R.id.play_button).setOnClickListener(v -> {
            openGooglePlay(DownloadActivity.this, info.getItem().getPackageName());
            trackEvent("click-google-play");
        });
        metaContainer.setOnClickListener(v -> editMeta());
        findViewById(R.id.submit_rating).setOnClickListener(v -> submitRating());
        findViewById(R.id.rating_retry_button).setOnClickListener(v -> submitRating());
        View.OnClickListener checksumClickListener = v -> {
            StringUtil.copyStringToClipboard(
                    DownloadActivity.this,
                    checksumView.getText().toString(),
                    R.string.checksum_copied);
            trackEvent("copy-checksum");
        };
        findViewById(R.id.app_checksum_title).setOnClickListener(checksumClickListener);
        checksumView.setOnClickListener(checksumClickListener);
        ratingContainer.setOnClickListener(v -> RatingsActivity_.intent(DownloadActivity.this).appId(appId).start());
        uploaderContainerView.setOnClickListener(v -> {
            long userId = info.getItem().getUserId();
            if (userId > 0) {
                ProfileActivity_.intent(DownloadActivity.this).userId(userId).start();
            }
        });
        if (moderation) {
            findViewById(R.id.moderation_block).setVisibility(View.VISIBLE);

            View.OnClickListener approveClickListener = v -> {
                submitModerationDecision(true);
                trackEvent("approve-app");
            };
            findViewById(R.id.button_approve).setOnClickListener(approveClickListener);

            View.OnClickListener denyClickListener = v -> {
                submitModerationDecision(false);
                trackEvent("deny-app");
            };
            findViewById(R.id.button_deny).setOnClickListener(denyClickListener);
        }

        if (isCreateInstance) {
            loadInfo();
            trackEvent("open-download-screen");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);
        if (!canUnlink()) {
            menu.removeItem(R.id.unlink);
        }
        if (!canUnpublish()) {
            menu.removeItem(R.id.unpublish);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Runnable runnable;
                if (finishOnly) {
                    runnable = null;
                } else {
                    runnable = this::openStore;
                }
                finishAttempt(runnable);
                break;
            }
            case R.id.unlink: {
                UnlinkActivity_
                        .intent(this)
                        .appId(appId)
                        .label(appLabel)
                        .startForResult(REQUEST_CODE_UNLINK);
                trackEvent("unlink-app");
                break;
            }
            case R.id.unpublish: {
                UnpublishActivity_
                        .intent(this)
                        .appId(appId)
                        .label(appLabel)
                        .startForResult(REQUEST_CODE_UNLINK);
                trackEvent("unpublish-app");
                break;
            }
            case R.id.abuse: {
                AbuseActivity_
                        .intent(this)
                        .appId(appId)
                        .label(appLabel)
                        .start();
                trackEvent("abuse-app");
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finishAttempt(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DownloadController.getInstance().onAttach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        DownloadController.getInstance().onDetach(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindButtons();
        bindRateContainer();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STORE_APP_ID, appId);
        outState.putString(STORE_APP_PACKAGE, appPackage);
        outState.putString(STORE_APP_LABEL, appLabel);
        outState.putBoolean(STORE_MODERATION, moderation);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPDATE_META) {
            reloadInfo();
        } else if (requestCode == REQUEST_CODE_UNLINK) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    private void loadInfo() {
        DownloadController.getInstance().loadInfo(appId, appPackage);
    }

    private void reloadInfo() {
        if (!DownloadController.getInstance().isStarted()) {
            loadInfo();
        }
    }

    private void openStore() {
        Intent intent = new Intent(DownloadActivity.this, HomeActivity.class)
                .setAction(HomeActivity.ACTION_STORE)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private void bindStoreItem(StoreInfo info) {
        this.info = info;
        StoreItem item = info.getItem();
        Meta meta = info.getMeta();
        fetch(iconView, item.getIcon(), imageViewHandlers -> {
            centerCrop(imageViewHandlers);
            withPlaceholder(imageViewHandlers, R.drawable.app_placeholder);
            imageViewHandlers.setPlaceholder(imageViewViewHolder -> {
                imageViewViewHolder.get().setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageViewViewHolder.get().setImageResource(R.drawable.app_placeholder);
                return null;
            });
            return null;
        });
        String sizeText;
        int sizeFactor;
        long bytes = item.getSize();
        if (bytes < 1024 * 1024) {
            sizeText = String.format("%d", bytes / 1024);
            sizeFactor = R.string.kilobytes;
        } else if (bytes < 10 * 1024 * 1024) {
            sizeText = String.format("%.1f", bytes / 1024.0f / 1024.0f);
            sizeFactor = R.string.megabytes;
        } else {
            sizeText = String.format("%d", bytes / 1024 / 1024);
            sizeFactor = R.string.megabytes;
        }
        labelView.setText(LocaleHelper.getLocalizedLabel(item));
        packageView.setText(item.getPackageName());
        downloadsView.setCount(String.valueOf(item.getDownloads()));
        sizeView.setCount(sizeText);
        sizeView.setDescription(getString(sizeFactor));
        minAndroidView.setCount(item.getAndroidVersion());
        if (TextUtils.isEmpty(meta.getDescription())) {
            metaBlockView.setVisibility(View.GONE);
        } else {
            metaBlockView.setVisibility(View.VISIBLE);
            descriptionView.setText(info.getMeta().getDescription());
            descriptionAuthorAvatar.setMemberId(info.getMeta().getUserId());
        }
        Category category = meta.getCategory();
        if (category != null && category.getId() != 0) {
            categoryContainer.setVisibility(View.VISIBLE);
            try {
                SVG svg = SVG.getFromString(category.getIcon());
                categoryIcon.setSVG(svg);
            } catch (SVGParseException ignored) {
            }
            categoryTitle.setText(getLocalizedName(meta.getCategory()));
        } else {
            categoryContainer.setVisibility(View.GONE);
        }
        if (item.getUserId() > 0) {
            uploaderContainerView.setVisibility(View.VISIBLE);
            uploaderAvatar.setMemberId(item.getUserId());
        } else {
            uploaderContainerView.setVisibility(View.GONE);
        }
        setRating(meta.getRating(), meta.getRateCount(), meta.getScores());
        versionView.setText(getString(R.string.app_version_format, item.getVersion(), item.getVersionCode()));
        uploadedTimeView.setText(timeHelper().getFormattedDate(SECONDS.toMillis(item.getTime())));
        checksumView.setText(item.getSha1());
        bindButtons(item.getPackageName(), item.getVersionCode());
        bindPermissions(item.getPermissions());
        bindVersions(info.getVersions(), item.getAppId(), item.getVersionCode());
        bindRatingItems(info.getRates());
        bindUserRating(info.getUserRating());
        appLabel = labelView.getText().toString();
        setTitle(appLabel);

        bindRateContainer();

        exclusiveBadge.setVisibility(info.getMeta().isExclusive() ? View.VISIBLE : View.GONE);
        invalidateOptionsMenu();

        boolean isCanEditMeta = canEditMeta();
        metaContainer.setClickable(isCanEditMeta);
        editMeta.setVisibility(isCanEditMeta ? View.VISIBLE : View.GONE);
    }

    private void bindButtons() {
        if (info != null) {
            StoreItem item = info.getItem();
            bindButtons(item.getPackageName(), item.getVersionCode());
        }
    }

    private void bindButtons(final String packageName, int versionCode) {
        if (DownloadController.getInstance().isDownloading()) {
            buttonsSwitcher.setDisplayedChild(2);
            return;
        }
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            if (packageInfo != null) {
                final Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);
                boolean isRunnable = launchIntent != null;
                boolean isNewer = versionCode > packageInfo.versionCode;
                if (isRunnable) {
                    buttonsSwitcher.setDisplayedChild(1);
                    buttonFirst.setText(R.string.remove);
                    buttonFirst.setOnClickListener(v -> requestAppRemoval(packageName));
                    if (isNewer) {
                        buttonSecond.setText(R.string.update);
                        buttonSecond.setOnClickListener(v -> updateApp());
                    } else {
                        buttonSecond.setText(R.string.open);
                        buttonSecond.setOnClickListener(v -> openApp(launchIntent));
                    }
                } else {
                    buttonsSwitcher.setDisplayedChild(0);
                    if (isNewer) {
                        buttonOne.setText(R.string.update);
                        buttonOne.setOnClickListener(v -> updateApp());
                    } else {
                        buttonOne.setText(R.string.remove);
                        buttonOne.setOnClickListener(v -> requestAppRemoval(packageName));
                    }
                }
            }
            return;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        buttonsSwitcher.setDisplayedChild(0);
        buttonOne.setText(R.string.install);
        buttonOne.setOnClickListener(v -> checkPermissionsForInstall());
    }

    private void responsibilityDenial() {
        if (PreferenceHelper.isShowResponsibilityDenial(this)) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.responsibility_denial_title))
                    .setMessage(getString(R.string.responsibility_denial_text))
                    .setNegativeButton(R.string.yes, (dialog, which) -> {
                        PreferenceHelper.setShowResponsibilityDenial(DownloadActivity.this, false);
                        checkAndroidVersion();
                    })
                    .setPositiveButton(R.string.no, (dialog, which) -> showError(R.string.agree_with_responsibility_condition))
                    .show();
        } else {
            checkAndroidVersion();
        }
    }

    private void checkPermissionsForInstall() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    responsibilityDenial();
                } else {
                    showError(R.string.write_permission_install);
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = DownloadActivity.this.getString(R.string.app_name);
                String message = DownloadActivity.this.getString(R.string.write_permission_install);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void checkAndroidVersion() {
        if (info.info.getSdkVersion() > Build.VERSION.SDK_INT) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.unsupported_sdk_title))
                    .setMessage(getString(R.string.unsupported_sdk_text, info.info.getAndroidVersion()))
                    .setNegativeButton(R.string.yes, (dialog, which) -> installApp())
                    .setPositiveButton(R.string.no, (dialog, which) -> {
                    })
                    .show();
        } else {
            installApp();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void installApp() {
        File directory = getExternalDirectory();
        File destination = new File(directory, getApkName(info.getItem()));
        String filePath = destination.getAbsolutePath();
        if (destination.exists() && destination.length() == info.getItem().getSize()) {
            onDownloaded(filePath);
        } else {
            destination.delete();
            DownloadController.getInstance().download(info.getLink(), filePath);
        }
        trackEvent("install-app");
    }

    private void updateApp() {
        checkPermissionsForInstall();
    }

    private void cancelDownload() {
        DownloadController.getInstance().cancelDownload();
    }

    private void removeApp(String packageName) {
        Uri packageUri = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
        startActivity(uninstallIntent);
        trackEvent("remove-app");
    }

    private void requestAppRemoval(final String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                @Override
                public void onPermissionResult(Permiso.ResultSet resultSet) {
                    if (resultSet.areAllPermissionsGranted()) {
                        removeApp(packageName);
                    } else {
                        showError(R.string.request_delete_packages);
                    }
                }

                @Override
                public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                    String title = DownloadActivity.this.getString(R.string.app_name);
                    String message = DownloadActivity.this.getString(R.string.request_delete_packages);
                    Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
                }
            }, Manifest.permission.REQUEST_DELETE_PACKAGES);
        } else {
            removeApp(packageName);
        }
    }

    private void openApp(Intent launchIntent) {
        startActivity(launchIntent);
    }

    private void bindPermissions(final List<String> permissions) {
        final boolean hasPermissions = !permissions.isEmpty();
        int count = Math.min(MAX_PERMISSIONS_COUNT, permissions.size());
        permissionsContainer.removeAllViews();
        for (int c = 0; c < count; c++) {
            String permission = permissions.get(c);
            View permissionView = getLayoutInflater().inflate(R.layout.permission_view, permissionsContainer, false);
            TextView permissionDescription = permissionView.findViewById(R.id.permission_description);
            TextView permissionName = permissionView.findViewById(R.id.permission_name);
            String description = getPermissionSmallInfo(this, permission).getDescription();
            permissionDescription.setText(description);
            permissionName.setText(permission);
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams) permissionView.getLayoutParams();
            layoutParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.permissions_margin);
            permissionView.setLayoutParams(layoutParams);
            permissionsContainer.addView(permissionView);
        }
        permissionsBlock.setVisibility(hasPermissions ? View.VISIBLE : View.GONE);
        boolean isOverflow = permissions.size() > MAX_PERMISSIONS_COUNT;
        readMoreButton.setVisibility(hasPermissions && isOverflow ? View.VISIBLE : View.GONE);
        shadowView.setVisibility(readMoreButton.getVisibility());
        if (isOverflow) {
            permissionsBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PermissionsActivity_.intent(DownloadActivity.this)
                            .permissions(new PermissionsList(new ArrayList<>(permissions)))
                            .start();
                }
            });
        }

        int stringRes;
        String access = "";
        boolean hasSmsAccess = false;
        boolean hasGeoAccess = false;
        boolean hasCallAccess = false;
        for (String permission : permissions) {
            String permissionUpper = permission.toUpperCase(Locale.getDefault());
            boolean isDangerous = PermissionHelper.getPermissionSmallInfo(this, permission).isDangerous();
            if (isDangerous) {
                if (!hasSmsAccess && permissionUpper.contains("SMS")) {
                    stringRes = R.string.access_sms;
                    hasSmsAccess = true;
                } else if (!hasGeoAccess && permissionUpper.contains("LOCATION")) {
                    stringRes = R.string.access_geo;
                    hasGeoAccess = true;
                } else if (!hasCallAccess && permissionUpper.contains("CALL")) {
                    stringRes = R.string.access_call;
                    hasCallAccess = true;
                } else {
                    stringRes = 0;
                }
                if (stringRes != 0) {
                    if (!TextUtils.isEmpty(access)) {
                        access += ", ";
                    }
                    access += getString(stringRes);
                }
            }
        }
        extraAccess.setVisibility(TextUtils.isEmpty(access) ? View.GONE : View.VISIBLE);
        extraAccess.setText(getString(R.string.has_access, access));
    }

    @SuppressLint("SetTextI18n")
    private void bindVersions(List<StoreVersion> versions, String appId, int versionCode) {
        versionsContainer.removeAllViews();
        boolean isVersionsAdded = false;
        for (final StoreVersion version : versions) {
            if (TextUtils.equals(version.getAppId(), appId)) {
                continue;
            }
            View versionView = getLayoutInflater().inflate(R.layout.version_view, versionsContainer, false);
            TextView versionNameView = versionView.findViewById(R.id.app_version_name);
            TextView versionCodeView = versionView.findViewById(R.id.app_version_code);
            TextView versionDownloads = versionView.findViewById(R.id.app_version_downloads);
            TextView newerBadge = versionView.findViewById(R.id.app_newer_badge);
            versionNameView.setText(version.getVerName());
            versionCodeView.setText('(' + String.valueOf(version.getVerCode()) + ')');
            versionDownloads.setText(String.valueOf(version.getDownloads()));
            boolean isNewer = version.getVerCode() > versionCode;
            boolean isSame = version.getVerCode() == versionCode;
            if (isNewer) {
                newerBadge.setVisibility(View.VISIBLE);
                newerBadge.setText(R.string.newer);
            } else if (isSame) {
                newerBadge.setVisibility(View.VISIBLE);
                newerBadge.setText(R.string.same);
            } else {
                newerBadge.setVisibility(View.GONE);
            }
            versionView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishAttempt(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(DownloadActivity.this, DownloadActivity.class);
                            intent.putExtra(DownloadActivity.STORE_APP_ID, version.getAppId());
                            intent.putExtra(DownloadActivity.STORE_APP_LABEL, LocaleHelper.getLocalizedLabel(info.getItem()));
                            startActivity(intent);
                        }
                    });
                }
            });
            versionsContainer.addView(versionView);
            isVersionsAdded = true;
        }
        versionsContainer.setVisibility(isVersionsAdded ? View.VISIBLE : View.GONE);
        otherVersionsTitle.setVisibility(versionsContainer.getVisibility());
    }

    private void bindRatingItems(List<RatingItem> ratingItems) {
        ratingItemsContainer.removeAllViews();
        boolean isRatingsAdded = false;
        for (final RatingItem ratingItem : ratingItems) {
            View ratingItemView = getLayoutInflater().inflate(R.layout.rating_item, ratingItemsContainer, false);
            MemberImageView memberImageView = ratingItemView.findViewById(R.id.member_avatar);
            AppCompatRatingBar ratingView = ratingItemView.findViewById(R.id.rating_view);
            TextView dateView = ratingItemView.findViewById(R.id.date_view);
            TextView commentView = ratingItemView.findViewById(R.id.comment_view);

            tintRatingIndicator(this, ratingView);

            memberImageView.setMemberId(ratingItem.getUserId());
            ratingView.setRating(ratingItem.getScore());
            dateView.setText(timeHelper().getFormattedDate(SECONDS.toMillis(ratingItem.getTime())));
            String text = ratingItem.getText();
            commentView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
            commentView.setText(text);
            ratingItemsContainer.addView(ratingItemView);
            isRatingsAdded = true;
        }
        ratingItemsContainer.setVisibility(isRatingsAdded ? View.VISIBLE : View.GONE);
    }

    private void bindRateContainer() {
        if (info != null) {
            long userId = Session.getInstance().getUserData().getUserId();
            ratingMemberAvatar.setMemberId(userId);

            StoreItem item = info.getItem();
            boolean isInstalled = false;
            PackageManager packageManager = getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(item.getPackageName(), 0);
                if (packageInfo != null) {
                    isInstalled = true;
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            boolean canRate = item.getUserId() != userId && isInstalled;
            rateContainer.setVisibility(canRate ? View.VISIBLE : View.GONE);
        }
    }

    private void bindUserRating(UserRating rating) {
        String text = "";
        int score = 0;
        if (rating != null) {
            text = rating.getText();
            score = rating.getScore();
        }
        userOpinion.setText(text);
        userRating.setRating(score);
        int emptyColor = getAttributedColor(this, R.attr.rating_empty);
        int fillColor = getResources().getColor(R.color.accent_color);
        tintRatingIndicator(userRating, emptyColor, fillColor);
    }

    @Override
    public void onInfoLoaded(StoreInfo storeInfo) {
        if (TextUtils.isEmpty(appId)) {
            appId = storeInfo.getItem().getAppId();
        }
        bindStoreItem(storeInfo);
        viewFlipper.setDisplayedChild(1);
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
        if (abuseItem != null) {
            abuseItem.setVisible(true);
        }
    }

    @Override
    public void onInfoError() {
        errorText.setText(R.string.store_info_error);
        viewFlipper.setDisplayedChild(2);
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onFileNotFound() {
        errorText.setText(R.string.store_not_found);
        viewFlipper.setDisplayedChild(2);
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onInfoProgress() {
        if (!swipeRefresh.isRefreshing()) {
            viewFlipper.setDisplayedChild(0);
        }
    }

    public void onModerationProgress() {
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
        viewFlipper.setDisplayedChild(0);
    }

    public void onModerationDone() {
        viewFlipper.setDisplayedChild(1);
        swipeRefresh.setEnabled(true);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    public void onDownloadStarted() {
        buttonsSwitcher.setDisplayedChild(2);
        swipeRefresh.setEnabled(false);
        progress.setIndeterminate(true);
    }

    @Override
    public void onDownloadProgress(long downloadedBytes) {
        progress.setIndeterminate(false);
        StoreItem item = info.getItem();
        long time = System.currentTimeMillis();
        if (item.getSize() > 0 && time - progressUpdateTime >= DEBOUNCE_DELAY) {
            progressUpdateTime = time;
            progress.setProgress((int) (100 * downloadedBytes / item.getSize()));
        }
    }

    @Override
    public void onDownloaded(String filePath) {
        viewFlipper.setDisplayedChild(1);
        swipeRefresh.setEnabled(true);
        bindButtons();

        IntentHelper.openFile(this, filePath, "application/vnd.android.package-archive");
    }

    @Override
    public void onDownloadError() {
        showError(R.string.downloading_error);
        viewFlipper.setDisplayedChild(1);
        swipeRefresh.setEnabled(true);
        bindButtons();
    }

    private void showError(@StringRes int message) {
        Snackbar.make(viewFlipper, message, Snackbar.LENGTH_LONG).show();
    }

    private static String getApkPrefix(StoreItem item) {
        return FileHelper.escapeFileSymbols(item.getLabel() + "-" + item.getVersion() + "-" + item.getAppId());
    }

    private static String getApkSuffix() {
        return ".apk";
    }

    private static String getApkName(StoreItem item) {
        return getApkPrefix(item) + getApkSuffix();
    }

    private void finishAttempt(final Runnable runnable) {
        if (DownloadController.getInstance().isDownloading()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.cancel_download_title))
                    .setMessage(getString(R.string.cancel_download_text))
                    .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelDownload();
                            finish();
                            if (runnable != null) {
                                runnable.run();
                            }
                        }
                    })
                    .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            finish();
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    private void editMeta() {
        StoreItem item = info.getItem();
        Intent intent = new Intent(this, MetaActivity.class)
                .putExtra(MetaActivity.APP_ID_EXTRA, appId)
                .putExtra(MetaActivity.STORE_ITEM_EXTRA, item);
        startActivityForResult(intent, REQUEST_UPDATE_META);
    }

    private void submitRating() {
        try {
            int score = (int) userRating.getRating();
            String text = userOpinion.getText().toString();
            String guid = Session.getInstance().getUserData().getGuid();
            if (score < 1) {
                Toast.makeText(this, R.string.please_set_rating, Toast.LENGTH_LONG).show();
                return;
            }
            showRatingProgress();
            Call<ApiResponse<RateResponse>> call = serviceHolder.getService().setRating(1, appId, guid, score, text);
            call.enqueue(new Callback<ApiResponse<RateResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<RateResponse>> call, final Response<ApiResponse<RateResponse>> response) {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful() &&
                                    response.body() != null &&
                                    response.body().getStatus() == 200) {
                                onRatingPosted();
                            } else {
                                showRatingError();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<ApiResponse<RateResponse>> call, Throwable t) {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            showRatingError();
                        }
                    });
                }
            });
            trackEvent("submit-rating");
        } catch (Throwable ex) {
            showRatingError();
        }
    }

    private void onRatingPosted() {
        reloadInfo();
        ratingFlipper.setDisplayedChild(0);
    }

    private void showRatingProgress() {
        KeyboardHelper.hideKeyboard(userOpinion);
        ratingFlipper.setDisplayedChild(1);
    }

    private void showRatingError() {
        ratingFlipper.setDisplayedChild(2);
    }

    private void tintProgress(ProgressBar progressBar, int color) {
        LayerDrawable progress = (LayerDrawable) progressBar.getProgressDrawable();
        progress.getDrawable(1).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    @SuppressLint("SetTextI18n")
    private void setRating(float rating, int count, Scores scores) {
        boolean hasRating = (rating != 0 && count > 0);
        ratingContainer.setVisibility(hasRating ? View.VISIBLE : View.GONE);
        if (hasRating) {
            smallRatingIndicator.setRating(rating);
            ratingScore.setText(Float.toString(rating));
            ratesCount.setText(Integer.toString(count));

            int maxValue = getMaxValue(
                    scores.getFive(),
                    scores.getFour(),
                    scores.getThree(),
                    scores.getTwo(),
                    scores.getOne()
            );

            rdeFive.setProgress(100 * scores.getFive() / maxValue);
            rdeFour.setProgress(100 * scores.getFour() / maxValue);
            rdeThree.setProgress(100 * scores.getThree() / maxValue);
            rdeTwo.setProgress(100 * scores.getTwo() / maxValue);
            rdeOne.setProgress(100 * scores.getOne() / maxValue);
        }
    }

    private int getMaxValue(int... values) {
        int result = values[0];
        for (int value : values) {
            result = Math.max(result, value);
        }
        return result;
    }

    private boolean canUnlink() {
        return info != null
                && info.actions != null
                && Arrays.asList(info.actions).contains(UNLINK_ACTION);
    }

    private boolean canUnpublish() {
        return info != null
                && info.actions != null
                && Arrays.asList(info.actions).contains(UNPUBLISH_ACTION);
    }

    private boolean canEditMeta() {
        return info != null
                && info.actions != null
                && Arrays.asList(info.actions).contains(EDIT_META_ACTION);
    }

    private void submitModerationDecision(boolean approve) {
        String guid = Session.getInstance().getUserData().getGuid();
        onModerationProgress();
        Call<StoreResponse<ModerationResponse>> call = serviceHolder.getService().setModerationDecision(guid, appId, approve ? 1 : -1);
        call.enqueue(new Callback<StoreResponse<ModerationResponse>>() {
            @Override
            public void onResponse(Call<StoreResponse<ModerationResponse>> call, final Response<StoreResponse<ModerationResponse>> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful() &&
                                response.body() != null &&
                                response.body().getStatus() == 200) {
                            Toast.makeText(DownloadActivity.this, R.string.moderation_done, Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(DownloadActivity.this, R.string.moderation_error, Toast.LENGTH_SHORT).show();
                            onModerationDone();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<StoreResponse<ModerationResponse>> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DownloadActivity.this, R.string.moderation_error, Toast.LENGTH_SHORT).show();
                        onModerationDone();
                    }
                });
            }
        });
        trackEvent("submit-moderation-decision");
    }

    public static Intent createAppActivityIntent(Context context, String appId, String title, boolean moderation, boolean finishOnly) {
        Intent intent = new Intent(context, DownloadActivity.class);
        intent.putExtra(DownloadActivity.STORE_APP_ID, appId);
        intent.putExtra(DownloadActivity.STORE_APP_LABEL, title);
        intent.putExtra(DownloadActivity.STORE_MODERATION, moderation);
        intent.putExtra(DownloadActivity.STORE_FINISH_ONLY, finishOnly);
        return intent;
    }

}
