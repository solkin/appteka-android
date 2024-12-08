package com.tomclaw.appsend.main.store.search;

import static com.tomclaw.appsend.util.ThemesKt.updateTheme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.util.ThemesKt;

@SuppressLint("Registered")
public class SearchActivity extends AppCompatActivity {

    Toolbar toolbar;

    EditText queryEdit;

    SearchFragment searchFragment;

    LegacyInjector legacyInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        updateTheme(this);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            legacyInjector.analytics.trackEvent("open-search-screen");
        }
    }

    void init() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.search_app));
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        queryEdit.setText(searchFragment.query);
        queryEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchFragment != null) {
                    searchFragment.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        queryEdit.requestFocus();
    }

    boolean actionHome() {
        onBackPressed();
        return true;
    }

    public static Intent createSearchActivityIntent(Context context) {
        return SearchActivity_.intent(context).get();
    }

}
