package com.tomclaw.appsend;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tomclaw.appsend.main.adapter.PermissionsAdapter;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.EdgeChanger;
import com.tomclaw.appsend.util.ThemeHelper;

import java.util.List;

/**
 * Created by ivsolkin on 27.01.17.
 */
public class PermissionsActivity extends AppCompatActivity {

    public static String EXTRA_PERMISSIONS = "permissions";

    private RecyclerView recyclerView;
    private PermissionsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.permissions_activity);
        ThemeHelper.updateStatusBar(this);

        List<String> permissions = getIntent().getStringArrayListExtra(EXTRA_PERMISSIONS);
        if (permissions == null || permissions.isEmpty()) {
            finish();
            return;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        setTitle(R.string.required_permissions);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
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

        adapter = new PermissionsAdapter(this, permissions);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
        }
        return true;
    }
}
