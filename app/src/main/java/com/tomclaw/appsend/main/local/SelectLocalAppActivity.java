package com.tomclaw.appsend.main.local;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.di.legacy.LegacyInjector;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.util.ThemeHelper;

import java.util.Arrays;
import java.util.List;

@SuppressLint("Registered")
public class SelectLocalAppActivity extends PermisoActivity implements CommonItemClickListener {

    public static final String SELECTED_ITEM = "selected_item";

    Toolbar toolbar;

    ViewPager pager;

    TabLayout tabs;

    LegacyInjector legacyInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            legacyInjector.analytics.trackEvent("open-select-app-screen");
        }
    }

    void init() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        List<Pair<String, Fragment>> fragments = Arrays.asList(
                new Pair<>(
                        getString(R.string.nav_installed),
                        new SelectInstalledFragment_()
                ),
                new Pair<>(
                        getString(R.string.nav_distro),
                        new SelectDistroFragment_()
                )
        );

        LocalAppsPagerAdapter adapter = new LocalAppsPagerAdapter(getSupportFragmentManager(), fragments);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
    }

    boolean actionHome() {
        leaveScreen();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        leaveScreen();
    }

    @Override
    public void onClick(final CommonItem item) {
        leaveScreen(item);
    }

    private void showError(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void leaveScreen() {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void leaveScreen(CommonItem item) {
        Intent data = new Intent();
        data.putExtra(SELECTED_ITEM, item);
        setResult(RESULT_OK, data);
        finish();
    }

    public static class LocalAppsPagerAdapter extends FragmentStatePagerAdapter {

        private final List<Pair<String, Fragment>> fragments;

        LocalAppsPagerAdapter(FragmentManager fm, List<Pair<String, Fragment>> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = fragments.get(position).second;
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments.get(position).first;
        }
    }

    public static Intent createSelectAppActivity(Context context) {
        return SelectLocalAppActivity_.intent(context).get();
    }

}
