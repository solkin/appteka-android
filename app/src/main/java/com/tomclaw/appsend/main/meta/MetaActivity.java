package com.tomclaw.appsend.main.meta;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.GlideApp;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.main.item.StoreItem;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.KeyboardHelper;
import com.tomclaw.appsend.util.LocaleHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Application meta editor screen
 * Created by solkin on 23.09.17.
 */
@EActivity(R.layout.meta_activity)
@OptionsMenu(R.menu.meta_menu)
public class MetaActivity extends AppCompatActivity {

    @Bean
    StoreServiceHolder serviceHolder;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    Spinner categories;

    @ViewById
    CheckBox exclusive;

    @ViewById
    EditText description;

    @ViewById
    ImageView appIcon;

    @ViewById
    TextView appLabel;

    @ViewById
    TextView appPackage;

    @ViewById
    TextView errorText;

    @ViewById
    Button retryButton;

    @Extra
    String appId;

    @Extra
    StoreItem storeItem;

    @Extra
    CommonItem commonItem;

    @InstanceState
    MetaResponse meta;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

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

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    @OptionsItem(R.id.menu_save)
    void onSaveMeta() {
        saveMeta();
    }

    private void updateHeader() {
        if (commonItem != null) {
            PackageInfo packageInfo = commonItem.getPackageInfo();

            if (packageInfo != null) {
                GlideApp.with(this)
                        .load(packageInfo)
                        .into(appIcon);
            }

            appLabel.setText(commonItem.getLabel());
            appPackage.setText(commonItem.getPackageName());
        } else {
            GlideApp.with(this)
                    .load(storeItem.getIcon())
                    .into(appIcon);

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
}
