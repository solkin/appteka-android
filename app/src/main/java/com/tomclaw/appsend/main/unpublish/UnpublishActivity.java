package com.tomclaw.appsend.main.unpublish;

import static com.tomclaw.appsend.util.ThemesKt.updateTheme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by solkin on 19/03/2018.
 */
@SuppressLint("Registered")
public class UnpublishActivity extends AppCompatActivity {

    Toolbar toolbar;

    ViewFlipper viewFlipper;

    EditText reasonInput;

    String label;

    String appId;

    StoreServiceHolder serviceHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    void init() {
        toolbar.setBackgroundColor(getResources().getColor(R.color.unlink_color));
        setSupportActionBar(toolbar);
        ActionBar actionBar;
        if ((actionBar = getSupportActionBar()) != null) {
            actionBar.setTitle(getString(R.string.unpublish_of, label));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        int color = getResources().getColor(R.color.unlink_color);
        StatusBarUtil.setColor(this, color);

        onReady();
    }

    boolean actionHome() {
        onBackPressed();
        return true;
    }

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
                Call<ApiResponse<UnpublishResponse>> call = serviceHolder.getService().unpublish(appId, reason);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UnpublishResponse>> call, final Response<ApiResponse<UnpublishResponse>> response) {
                        MainExecutor.execute(() -> {
                            if (response.isSuccessful()) {
                                onFileUnlinked();
                            } else {
                                onError();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UnpublishResponse>> call, Throwable t) {
                        MainExecutor.execute(() -> onError());
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
        Toast.makeText(this, R.string.thanks_for_attention, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        finish();
    }

    public void onError() {
        reasonInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
        showError(getString(R.string.unable_to_unlink_file));
    }

    public static Intent createUnpublishActivityIntent(Context context, String appId, String label) {
        return UnpublishActivity_.intent(context).appId(appId).label(label).get();
    }
}
