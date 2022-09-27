package com.tomclaw.appsend.main.meta;

import static com.tomclaw.imageloader.util.ImageViewHandlersKt.centerCrop;
import static com.tomclaw.imageloader.util.ImageViewHandlersKt.withPlaceholder;
import static com.tomclaw.imageloader.util.ImageViewsKt.fetch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.core.StoreServiceHolder_;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.main.unpublish.UnpublishActivity_;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.KeyboardHelper;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.PackageIconLoader;
import com.tomclaw.appsend.util.ThemeHelper;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Application meta editor screen
 * Created by solkin on 23.09.17.
 */
public class MetaActivity extends AppCompatActivity {

    public static final String APP_ID_EXTRA = "appId";
    public static final String STORE_ITEM_EXTRA = "storeItem";
    public static final String COMMON_ITEM_EXTRA = "commonItem";

    private StoreServiceHolder serviceHolder;

    private Toolbar toolbar;
    private ViewFlipper viewFlipper;
    private Spinner categories;
    private CheckBox exclusive;
    private EditText description;
    private ImageView appIcon;
    private TextView appLabel;
    private TextView appPackage;
    private TextView errorText;
    private Button retryButton;

    private String appId;
    private StoreItem storeItem;
    private CommonItem commonItem;

    private MetaResponse meta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        serviceHolder = StoreServiceHolder_.getInstance_(this);
        injectExtras();
        restoreSavedInstanceState(savedInstanceState);
        ThemeHelper.updateTheme(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.meta_activity);

        toolbar = findViewById(R.id.toolbar);
        viewFlipper = findViewById(R.id.view_flipper);
        categories = findViewById(R.id.categories);
        exclusive = findViewById(R.id.exclusive);
        description = findViewById(R.id.description);
        appIcon = findViewById(R.id.app_icon);
        appLabel = findViewById(R.id.app_label);
        appPackage = findViewById(R.id.app_package);
        errorText = findViewById(R.id.error_text);
        retryButton = findViewById(R.id.retry_button);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        updateHeader();

        if (meta == null) {
            loadMeta();
        } else {
            updateCategories();
            showContent();
        }
    }

    private void injectExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(APP_ID_EXTRA)) {
                this.appId = extras.getString(APP_ID_EXTRA);
            }
            if (extras.containsKey(STORE_ITEM_EXTRA)) {
                this.storeItem = extras.getParcelable(STORE_ITEM_EXTRA);
            }
            if (extras.containsKey(COMMON_ITEM_EXTRA)) {
                this.commonItem = extras.getParcelable(COMMON_ITEM_EXTRA);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelable("meta", meta);
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        meta = savedInstanceState.getParcelable("meta");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meta_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_save:
                saveMeta();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateHeader() {
        if (commonItem != null) {
            PackageInfo packageInfo = commonItem.getPackageInfo();

            if (packageInfo != null) {
                String uri = PackageIconLoader.getUri(packageInfo);
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
            }

            appLabel.setText(commonItem.getLabel());
            appPackage.setText(commonItem.getPackageName());
        } else {
            fetch(appIcon, storeItem.getIcon(), imageViewHandlers -> {
                centerCrop(imageViewHandlers);
                withPlaceholder(imageViewHandlers, R.drawable.app_placeholder);
                imageViewHandlers.setPlaceholder(imageViewViewHolder -> {
                    imageViewViewHolder.get().setScaleType(ImageView.ScaleType.CENTER_CROP);
                    imageViewViewHolder.get().setImageResource(R.drawable.app_placeholder);
                    return null;
                });
                return null;
            });

            appLabel.setText(LocaleHelper.getLocalizedLabel(storeItem));
            appPackage.setText(storeItem.getPackageName());
        }
    }

    private void loadMeta() {
        Call<ApiResponse<MetaResponse>> call = serviceHolder.getService().getMeta(1, appId, true);
        call.enqueue(new Callback<ApiResponse<MetaResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<MetaResponse>> call, final Response<ApiResponse<MetaResponse>> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            onMetaLoaded(response.body().getResult());
                        } else {
                            onMetaLoadingError();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<MetaResponse>> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onMetaLoadingError();
                    }
                });
            }
        });
    }

    private void saveMeta() {
        showProgress();
        KeyboardHelper.hideKeyboard(description);
        try {
            String guid = Session.getInstance().getUserData().getGuid();
            int position = categories.getSelectedItemPosition();
            int categoryId = (int) categories.getAdapter().getItemId(position);
            int exclusiveValue = exclusive.isChecked() ? 1 : 0;
            String descriptionText = description.getText().toString();
            Call<ApiResponse<MetaResponse>> call = serviceHolder.getService().setMeta(1, appId, guid, categoryId, exclusiveValue, descriptionText);
            call.enqueue(new Callback<ApiResponse<MetaResponse>>() {
                @Override
                public void onResponse(Call<ApiResponse<MetaResponse>> call, final Response<ApiResponse<MetaResponse>> response) {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            if (response.isSuccessful()) {
                                onMetaSaved();
                            } else {
                                onMetaSavingError();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<ApiResponse<MetaResponse>> call, Throwable t) {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            onMetaSavingError();
                        }
                    });
                }
            });
        } catch (Throwable ignored) {
            onMetaSavingError();
        }
    }

    private void onMetaLoaded(MetaResponse meta) {
        this.meta = meta;

        updateCategories();

        exclusive.setChecked(meta.getMeta().isExclusive());
        description.setText(meta.getMeta().getDescription());
        Category selectedCategory = meta.getMeta().getCategory();
        if (selectedCategory != null) {
            int selectedId = meta.getMeta().getCategory().getId();
            SpinnerAdapter adapter = categories.getAdapter();
            for (int c = 0; c < adapter.getCount(); c++) {
                if (adapter.getItemId(c) == selectedId) {
                    categories.setSelection(c);
                    break;
                }
            }
        }

        showContent();
    }

    private void updateCategories() {
        ArrayList<Category> categoriesList = new ArrayList<>();
        categoriesList.add(new Category());
        categoriesList.addAll(meta.getCategories());

        CategoriesAdapter adapter = new CategoriesAdapter(this, categoriesList);
        categories.setAdapter(adapter);
    }

    private void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    private void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

    private void onMetaLoadingError() {
        errorText.setText(R.string.load_meta_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMeta();
            }
        });
        viewFlipper.setDisplayedChild(2);
    }

    private void onMetaSaved() {
        setResult(RESULT_OK);
        finish();
    }

    private void onMetaSavingError() {
        errorText.setText(R.string.save_meta_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMeta();
            }
        });
        viewFlipper.setDisplayedChild(2);
    }

    public static Intent createEditMetaActivityIntent(Context context, String appId, String label, String icon, String packageName) {
        StoreItem item = new StoreItem(label, Collections.emptyMap(), icon, appId, 0,
                packageName, "", 0, 0, "",
                Collections.emptyList(), 0, 0, 0, 0,
                "", 0, null, 0, "");
        return new Intent(context, MetaActivity.class)
                .putExtra(MetaActivity.APP_ID_EXTRA, appId)
                .putExtra(MetaActivity.STORE_ITEM_EXTRA, item);
    }
}
