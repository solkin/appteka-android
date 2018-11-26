package com.tomclaw.appsend.main.auth;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

@SuppressLint("Registered")
@EActivity(R.layout.auth_activity)
public class AuthActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

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

    @Click(R.id.login)
    void onLoginClicked() {

    }

    @Click(R.id.register)
    void onRegisterClicked() {

    }

}
