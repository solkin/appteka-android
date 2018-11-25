package com.tomclaw.appsend.main.auth;

import android.annotation.SuppressLint;

import com.tomclaw.appsend.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

@SuppressLint("Registered")
@EActivity(R.layout.auth_activity)
public class AuthActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @Click(R.id.login)
    void onLoginClicked() {

    }

    @Click(R.id.register)
    void onRegisterClicked() {

    }

}
