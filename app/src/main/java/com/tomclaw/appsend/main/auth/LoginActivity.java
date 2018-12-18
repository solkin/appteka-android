package com.tomclaw.appsend.main.auth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.EditText;

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

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

@SuppressLint("Registered")
@EActivity(R.layout.login_activity)
public class LoginActivity extends AppCompatActivity {

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @ViewById
    Toolbar toolbar;

    @ViewById
    EditText emailInput;

    @ViewById
    EditText passwordInput;

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
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    @Click(R.id.login_button)
    void onLoginClicked() {
        String locale = getLocaleLanguage();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        login(locale, email, password);
    }

    @Click(R.id.register_button)
    void onRegisterClicked() {
        RegisterActivity_.intent(this).start();
    }

    private void login(String locale, String email, String password) {
        Call<LoginResponse> call = serviceHolder.getService()
                .login(1, locale, email, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, final Response<LoginResponse> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            onLoginSuccessful(response.body());
                        } else {
                            ResponseBody body = response.errorBody();
                            String description = getString(R.string.login_error);
                            if (body != null) {
                                try {
                                    description = GsonSingleton.getInstance()
                                            .fromJson(body.string(), LoginResponse.class)
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
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onError(getString(R.string.login_error));
                    }
                });
            }
        });
    }

    private void onLoginSuccessful(LoginResponse response) {
        session.getUserHolder().onUserRegistered(response.getGuid(), response.getUserId());
        setResult(RESULT_OK);
        finish();
    }

    private void onError(String description) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.error))
                .setMessage(description)
                .setPositiveButton(R.string.ok, null)
                .create()
                .show();
    }

}
