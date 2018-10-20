package com.tomclaw.appsend.main.local;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.util.Pair;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import com.greysonparrelli.permiso.PermisoActivity;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.CommonItem;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.List;

@SuppressLint("Registered")
@EActivity(R.layout.local_apps)
public class SelectLocalAppActivity extends PermisoActivity implements CommonItemClickListener {

    public static final String SELECTED_ITEM = "selected_item";

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewPager pager;

    @ViewById
    TabLayout tabs;

    @Extra
    DialogData dialogData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        List<Pair<String, Fragment>> fragments = Arrays.asList(
                new Pair<String, Fragment>(
                        getString(R.string.nav_installed),
                        new SelectInstalledFragment_()
                ),
                new Pair<String, Fragment>(
                        getString(R.string.nav_distro),
                        new SelectDistroFragment_()
                )
        );

        LocalAppsPagerAdapter adapter = new LocalAppsPagerAdapter(getSupportFragmentManager(), fragments);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        leaveScreen();
        return true;
    }

    @Override
    public void onBackPressed() {
        leaveScreen();
    }

    @Override
    public void onClick(final CommonItem item) {
        if (dialogData != null) {
            new AlertDialog.Builder(this)
                    .setTitle(dialogData.getTitle())
                    .setMessage(dialogData.getMessage())
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            leaveScreen(item);
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show();
        } else {
            leaveScreen(item);
        }
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

        private List<Pair<String, Fragment>> fragments;

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

}
