package com.tomclaw.appsend;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;

/**
 * Created by ivsolkin on 06.09.16.
 */

public class DonateActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private BillingProcessor bp;
    private View rootView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String licenseKey = getString(R.string.license_key);
        bp = new BillingProcessor(this, licenseKey, this);

        setContentView(R.layout.donate);

        rootView = findViewById(R.id.root_view);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        findViewById(R.id.donate_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onChocolateClicked();
            }
        });

        Utils.setupTint(this);
    }

    private void onChocolateClicked() {
        String chocolateId = getString(R.string.chocolate_id);
        bp.purchase(this, chocolateId);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        finish();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        finish();
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
