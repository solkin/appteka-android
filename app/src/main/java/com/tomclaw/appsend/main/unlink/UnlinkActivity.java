package com.tomclaw.appsend.main.unlink;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.flurry.android.FlurryAgent;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by solkin on 19/03/2018.
 */
@SuppressLint("Registered")
@EActivity(R.layout.unlink_activity)
@OptionsMenu(R.menu.unlink_menu)
public class UnlinkActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    EditText reasonInput;

    @Extra
    String label;

    @Extra
    String appId;

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        toolbar.setBackgroundColor(getResources().getColor(R.color.unlink_color));
        setSupportActionBar(toolbar);
        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setTitle(getString(R.string.unlink_of, label));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        int color = getResources().getColor(R.color.unlink_color);
        StatusBarUtil.setColor(this, color);

        onReady();

        FlurryAgent.logEvent("Unlink screen: open");
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    @OptionsItem(R.id.unlink)
    void onUnlink() {
        onUnlinkPressed();
    }

    private void onUnlinkPressed() {
        try {
            onProgress();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(reasonInput.getWindowToken(), 0);
            }

            String reason = reasonInput.getText().toString();
            if (!TextUtils.isEmpty(reason)) {
                String guid = session.getUserData().getGuid();
                Call<UnlinkResponse> call = serviceHolder.getService().unlink(1, guid, appId, reason);
                call.enqueue(new Callback<UnlinkResponse>() {
                    @Override
                    public void onResponse(Call<UnlinkResponse> call, final Response<UnlinkResponse> response) {
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (response.isSuccessful()) {
                                    onFileUnlinked();
                                } else {
                                    onError();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<UnlinkResponse> call, Throwable t) {
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                onError();
                            }
                        });
                    }
                });
            } else {
                showError(getString(R.string.fill_reason_field));
            }
        } catch (Throwable ex) {
            onError();
        }
    }

    private void showError(String message) {
        onReady();
        Snackbar.make(viewFlipper, message, Snackbar.LENGTH_LONG).show();
    }

    public void onReady() {
        reasonInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
    }

    public void onProgress() {
        reasonInput.setEnabled(false);
        viewFlipper.setDisplayedChild(1);
    }

    public void onFileUnlinked() {
        FlurryAgent.logEvent("Unlink screen: file unlinked");
        Toast.makeText(this, R.string.thanks_for_attention, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }

    public void onError() {
        reasonInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
        showError(getString(R.string.unable_to_unlink_file));
    }
}
