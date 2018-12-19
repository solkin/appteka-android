package com.tomclaw.appsend.main.auth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ViewFlipper;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.GsonSingleton;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tomclaw.appsend.util.KeyboardHelper.hideKeyboard;
import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

@SuppressLint("Registered")
@EActivity(R.layout.register_activity)
public class RegisterActivity extends AppCompatActivity {

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    EditText emailInput;

    @ViewById
    EditText passwordInput;

    @ViewById
    EditText nameInput;

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

        showContent();

        FlurryAgent.logEvent("Register screen: open");
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        setResult(RESULT_CANCELED);
        onBackPressed();
        return true;
    }

    @Click(R.id.register_button)
    void onRegisterClicked() {
        String guid = session.getUserData().getGuid();
        String locale = getLocaleLanguage();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String name = nameInput.getText().toString();
        register(guid, locale, email, password, name);
    }

    private void register(String guid, String locale, String email, String password, String name) {
        FlurryAgent.logEvent("Register screen: registration start");
        showProgress();
        Call<AuthResponse> call = serviceHolder.getService()
                .register(1, guid, locale, email, password, name);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, final Response<AuthResponse> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            AuthResponse body = response.body();
                            if (body != null) {
                                onRegistered(body);
                            } else {
                                onError();
                            }
                        } else {
                            ResponseBody body = response.errorBody();
                            String description = getString(R.string.register_error);
                            if (body != null) {
                                try {
                                    description = GsonSingleton.getInstance()
                                            .fromJson(body.string(), AuthResponse.class)
                                            .getDescription();
                                } catch (IOException ignored) {
                                }
                            }
                            onError(description);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onError();
                    }
                });
            }
        });
    }

    private void onRegistered(AuthResponse response) {
        FlurryAgent.logEvent("Register screen: registration successful");
        showContent();
        String guid = response.getGuid();
        long userId = response.getUserId();
        String email = response.getEmail();
        String name = response.getName();
        session.getUserHolder().onUserRegistered(guid, userId, email, name);
        setResult(RESULT_OK);
        finish();
    }

    private void onError() {
        onError(getString(R.string.register_error));
    }

    private void onError(String description) {
        FlurryAgent.logEvent("Register screen: registration error");
        showContent();
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(description)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

    private void showProgress() {
        viewFlipper.setDisplayedChild(0);
        hideKeyboard(emailInput);
        hideKeyboard(passwordInput);
        hideKeyboard(nameInput);
    }

    private void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

}
