package com.tomclaw.appsend;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.util.ThemeHelper;

/**
 * Created by ivsolkin on 06.09.16.
 */
public class DonateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donate);
        ThemeHelper.updateStatusBar(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.chocolate_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        findViewById(R.id.donate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChocolateClicked();
            }
        });

        int color = getResources().getColor(R.color.chocolate_color);
        StatusBarUtil.setColor(this, color);
    }

    private void onChocolateClicked() {
        String donateUrl = getString(R.string.donate_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(donateUrl)));
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return true;
    }
}
