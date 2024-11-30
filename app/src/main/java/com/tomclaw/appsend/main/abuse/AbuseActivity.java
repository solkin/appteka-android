package com.tomclaw.appsend.main.abuse;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;
import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.main.dto.AbuseResult;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.util.ThemeHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by solkin on 19.02.17.
 */
public class AbuseActivity extends AppCompatActivity {

    Toolbar toolbar;

    ViewFlipper viewFlipper;

    RadioGroup reasonGroup;

    EditText emailInput;

    String label;

    String appId;

    StoreServiceHolder serviceHolder;

    LegacyInjector legacyInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            legacyInjector.analytics.trackEvent("open-abuse-screen");
        }
    }

    void init() {
        toolbar.setBackgroundColor(getResources().getColor(R.color.abuse_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.abuse_on, label));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        int color = getResources().getColor(R.color.abuse_color);
        StatusBarUtil.setColor(this, color);

        onReady();
    }

    boolean actionHome() {
        onBackPressed();
        return true;
    }

    void onSendAbuse() {
        onSendPressed();
    }

    @SuppressLint("NonConstantResourceId")
    private void onSendPressed() {
        try {
            onProgress();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(emailInput.getWindowToken(), 0);

            int checked = reasonGroup.getCheckedRadioButtonId();
            String email = emailInput.getText().toString();
            if (checked >= 0 && !TextUtils.isEmpty(email)) {
                String reason;
                switch (checked) {
                    case R.id.does_not_works:
                        reason = "does_not_works";
                        break;
                    case R.id.malicious_app:
                        reason = "malicious_app";
                        break;
                    case R.id.license_violation:
                        reason = "license_violation";
                        break;
                    case R.id.private_app:
                        reason = "private_app";
                        break;
                    default:
                        showError(getString(R.string.unable_to_send_abuse));
                        return;
                }
                Call<ApiResponse<AbuseResult>> call = serviceHolder.getService().reportAbuse(appId, reason, email);
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AbuseResult>> call, final Response<ApiResponse<AbuseResult>> response) {
                        MainExecutor.execute(() -> {
                            if (response.isSuccessful()) {
                                onAbuseSent();
                            } else {
                                onError();
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AbuseResult>> call, Throwable t) {
                        MainExecutor.execute(() -> onError());
                    }
                });
            } else {
                showError(getString(R.string.fill_all_fields));
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
        emailInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
    }

    public void onProgress() {
        emailInput.setEnabled(false);
        viewFlipper.setDisplayedChild(1);
    }

    public void onAbuseSent() {
        Toast.makeText(this, R.string.thanks_for_attention, Toast.LENGTH_LONG).show();
        finish();
    }

    public void onError() {
        emailInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
        showError(getString(R.string.unable_to_send_abuse));
    }

    public static Intent createAbuseActivityIntent(Context context, String appId, String label) {
        return AbuseActivity_.intent(context).appId(appId).label(label).get();
    }
}
