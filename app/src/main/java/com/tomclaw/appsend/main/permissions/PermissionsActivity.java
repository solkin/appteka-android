package com.tomclaw.appsend.main.permissions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.PermissionsAdapter;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.EdgeChanger;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

/**
 * Created by ivsolkin on 27.01.17.
 */
@EActivity(R.layout.permissions_activity)
public class PermissionsActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    RecyclerView recyclerView;

    @Extra
    PermissionsList permissions;

    private PermissionsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterInject
    void checkExtra() {
        if (permissions == null || permissions.isEmpty()) {
            finish();
        }
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        setTitle(R.string.required_permissions);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        recyclerView.setItemAnimator(itemAnimator);
        final int toolbarColor = ColorHelper.getAttributedColor(this, R.attr.toolbar_background);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                EdgeChanger.setEdgeGlowColor(recyclerView, toolbarColor);
            }
        });

        adapter = new PermissionsAdapter(this, permissions.getList());
        recyclerView.setAdapter(adapter);
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        finish();
        return true;
    }

}
