package com.tomclaw.appsend.main.abuse;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.flurry.android.FlurryAgent;
import com.google.android.material.snackbar.Snackbar;
import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.AbuseResult;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by solkin on 19.02.17.
 */
@EActivity(R.layout.abuse_activity)
@OptionsMenu(R.menu.abuse_menu)
public class AbuseActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById(R.id.reason_group)
    RadioGroup reasonGroup;

    @ViewById(R.id.input_email)
    EditText emailInput;

    @Extra
    String label;

    @Extra
    String appId;

    @Bean
    StoreServiceHolder serviceHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        toolbar.setBackgroundColor(getResources().getColor(R.color.abuse_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.abuse_on, label));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        int color = getResources().getColor(R.color.abuse_color);
        StatusBarUtil.setColor(this, color);

        onReady();

        FlurryAgent.logEvent("Abuse screen: open");
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    @OptionsItem(R.id.send)
    void onSendAbuse() {
        onSendPressed();
    }

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
                Call<AbuseResult> call = serviceHolder.getService().reportAbuse(1, appId, reason, email);
                call.enqueue(new Callback<AbuseResult>() {
                    @Override
                    public void onResponse(Call<AbuseResult> call, final Response<AbuseResult> response) {
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (response.isSuccessful()) {
                                    onAbuseSent();
                                } else {
                                    onError();
                                }
                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<AbuseResult> call, Throwable t) {
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                onError();
                            }
                        });
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
        FlurryAgent.logEvent("Abuse screen: abuse sent");
        Toast.makeText(this, R.string.thanks_for_attention, Toast.LENGTH_LONG).show();
        finish();
    }

    public void onError() {
        emailInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
        showError(getString(R.string.unable_to_send_abuse));
    }
}
