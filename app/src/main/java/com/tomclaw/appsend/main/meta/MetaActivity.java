package com.tomclaw.appsend.main.meta;

import android.content.pm.PackageInfo;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.tomclaw.appsend.PackageIconGlideLoader;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by solkin on 23.09.17.
 */
@EActivity(R.layout.meta_activity)
public class MetaActivity extends AppCompatActivity {

    @Bean
    StoreServiceHolder serviceHolder;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewSwitcher viewSwitcher;

    @ViewById
    Spinner categories;

    @ViewById
    CheckBox exclusive;

    @ViewById
    TextView description;

    @ViewById
    ImageView appIcon;

    @ViewById
    TextView appLabel;

    @ViewById
    TextView appPackage;

    @Extra
    String appId;

    @Extra
    StoreItem storeItem;

    @Extra
    CommonItem commonItem;

    @InstanceState
    MetaResponse meta;

    private static PackageIconGlideLoader loader;

    @AfterInject
    void updateTheme() {
        ThemeHelper.updateTheme(this);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        updateHeader();

        if (meta == null) {
            loadMeta();
        } else {
            updateCategories();
            showContent();
        }
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    private void updateHeader() {
        if (commonItem != null) {
            PackageInfo packageInfo = commonItem.getPackageInfo();

            if (packageInfo != null) {
                if (loader == null) {
                    loader = new PackageIconGlideLoader(getPackageManager());
                }
                Glide.with(this)
                        .using(loader)
                        .load(packageInfo)
                        .into(appIcon);
            }

            appLabel.setText(commonItem.getLabel());
            appPackage.setText(commonItem.getPackageName());
        }
    }

    private void loadMeta() {
        Call<MetaResponse> call = serviceHolder.getService().getMeta(1, appId, true);
        call.enqueue(new Callback<MetaResponse>() {
            @Override
            public void onResponse(Call<MetaResponse> call, final Response<MetaResponse> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            onMetaLoaded(response.body());
                        } else {
                            onMetaLoadingError();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<MetaResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onMetaLoadingError();
                    }
                });
            }
        });
    }

    private void onMetaLoaded(MetaResponse meta) {
        this.meta = meta;

        updateCategories();

        exclusive.setChecked(meta.getMeta().isExclusive());
        description.setText(meta.getMeta().getDescription());
        Category selectedCategory = meta.getMeta().getCategory();
        if (selectedCategory != null) {
            int selectedId = meta.getMeta().getCategory().getId();
            int position = 0;
            for (Category category : meta.getCategories()) {
                if (category.getId() == selectedId) {
                    categories.setSelection(position);
                    break;
                }
                position++;
            }
        }

        showContent();
    }

    private void updateCategories() {
        CategoriesAdapter adapter = new CategoriesAdapter(this, meta.getCategories());
        categories.setAdapter(adapter);
    }

    private void showContent() {
        viewSwitcher.setDisplayedChild(1);
    }

    private void onMetaLoadingError() {
        // Show loading error with retry button.
    }
}
