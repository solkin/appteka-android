package com.tomclaw.appsend.main.donate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Created by ivsolkin on 06.09.16.
 */
@EActivity(R.layout.donate)
public class DonateActivity extends AppCompatActivity {

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

        toolbar.setBackgroundColor(getResources().getColor(R.color.chocolate_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        int color = getResources().getColor(R.color.chocolate_color);
        StatusBarUtil.setColor(this, color);
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        finish();
        return true;
    }

    @Click(R.id.donate_button)
    void onChocolateClicked() {
        String donateUrl = getString(R.string.donate_url);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(donateUrl)));
        } catch (Throwable ignored) {
        }
    }

}
