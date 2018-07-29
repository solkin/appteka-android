package com.tomclaw.appsend.main.local;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.Arrays;
import java.util.List;

@SuppressLint("Registered")
@EActivity(R.layout.local_apps)
public class LocalAppsActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewPager pager;

    @ViewById
    TabLayout tabs;

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
                new Pair<String, Fragment>(getString(R.string.nav_installed), new InstalledFragment_()),
                new Pair<String, Fragment>(getString(R.string.nav_distro), new DistroFragment_())
        );

        LocalAppsPagerAdapter adapter = new LocalAppsPagerAdapter(getSupportFragmentManager(), fragments);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        finish();
        return true;
    }

    public class LocalAppsPagerAdapter extends FragmentStatePagerAdapter {

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
