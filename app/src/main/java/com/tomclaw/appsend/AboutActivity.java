package com.tomclaw.appsend;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.jaeger.library.StatusBarUtil;

/**
 * Created by Solkin on 17.12.2014.
 */
public class AboutActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private static final String MARKET_DETAILS_URI = "market://details?id=";
    private static final String MARKET_DEVELOPER_URI = "market://search?q=";
    private static final String GOOGLE_PLAY_DETAILS_URI = "http://play.google.com/store/apps/details?id=";
    private static final String GOOGLE_PLAY_DEVELOPER_URI = "http://play.google.com/store/apps/search?q=";
    public static String DEVELOPER_NAME = "TomClaw Software";

    private BillingProcessor bp;
    private View rootView;
    private View presentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String licenseKey = getString(R.string.license_key);
        bp = new BillingProcessor(this, licenseKey, this);

        setContentView(R.layout.about_activity);

        rootView = findViewById(R.id.root_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);

        TextView appVersionView = (TextView) findViewById(R.id.app_version);
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            appVersionView.setText(getString(R.string.app_version, info.versionName, info.versionCode));
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        findViewById(R.id.rate_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rateApplication();
            }
        });

        findViewById(R.id.all_apps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allProjects();
            }
        });

        presentButton = findViewById(R.id.present_chocolate);
        presentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChocolateClicked();
            }
        });

        if (bp.loadOwnedPurchasesFromGoogle() &&
                bp.isPurchased(getString(R.string.chocolate_id))) {
            presentButton.setVisibility(View.GONE);
        }

        int color = getResources().getColor(R.color.action_bar_color);
        StatusBarUtil.setColor(this, color);
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
        }
        return true;
    }

    private void onChocolateClicked() {
        String chocolateId = getString(R.string.chocolate_id);
        bp.purchase(this, chocolateId);
    }

    private void rateApplication() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(MARKET_DETAILS_URI + appPackageName)));
        } catch (android.content.ActivityNotFoundException ignored) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_DETAILS_URI + appPackageName)));
        }
    }

    private void allProjects() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(MARKET_DEVELOPER_URI + DEVELOPER_NAME)));
        } catch (android.content.ActivityNotFoundException ignored) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_DEVELOPER_URI + DEVELOPER_NAME)));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        Toast.makeText(this, R.string.thank_you, Toast.LENGTH_LONG).show();
        presentButton.setVisibility(View.GONE);
    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        if (errorCode != Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            Snackbar.make(rootView, R.string.purchase_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBillingInitialized() {
    }
}
