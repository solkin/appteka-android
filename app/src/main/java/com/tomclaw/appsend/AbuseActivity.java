package com.tomclaw.appsend;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.jaeger.library.StatusBarUtil;
import com.tomclaw.appsend.main.controller.AbuseController;
import com.tomclaw.appsend.util.ThemeHelper;

/**
 * Created by solkin on 19.02.17.
 */
public class AbuseActivity extends AppCompatActivity implements AbuseController.AbuseCallback {

    public static final String APP_ID = "extra_app_id";
    public static final String APP_LABEL = "extra_app_label";

    private ViewFlipper viewFlipper;
    private RadioGroup reasonGroup;
    private EditText emailInput;

    private String label;
    private String appId;

    private AbuseController abuseController = AbuseController.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.abuse_activity);
        ThemeHelper.updateStatusBar(this);

        label = getIntent().getStringExtra(APP_LABEL);
        appId = getIntent().getStringExtra(APP_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.abuse_color));
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.abuse_on, label));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        reasonGroup = (RadioGroup) findViewById(R.id.reason_group);
        emailInput = (EditText) findViewById(R.id.input_email);

        int color = getResources().getColor(R.color.abuse_color);
        StatusBarUtil.setColor(this, color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.abuse_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.send: {
                onSendPressed();
                break;
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (abuseController.isStarted()) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.cancel_abuse_title))
                    .setMessage(getString(R.string.cancel_abuse_text))
                    .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelAbuse();
                            finish();
                        }
                    })
                    .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        abuseController.onAttach(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        abuseController.onDetach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        abuseController.resetAbuse();
    }

    private void cancelAbuse() {
        abuseController.cancelAbuse();
    }

    private void onSendPressed() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(emailInput.getWindowToken(), 0);

        int checked = reasonGroup.getCheckedRadioButtonId();
        String email = emailInput.getText().toString();
        if (checked >= 0 && !TextUtils.isEmpty(email)) {
            String reason;
            switch (checked) {
                case R.id.does_not_works:
                    reason = "does_not_works";
                    break;
                case R.id.malicious_app:
                    reason = "malicious_app";
                    break;
                case R.id.license_violation:
                    reason = "license_violation";
                    break;
                case R.id.private_app:
                    reason = "private_app";
                    break;
                default:
                    showError(getString(R.string.unable_to_send_abuse));
                    return;
            }
            abuseController.abuse(appId, reason, email);
        } else {
            showError(getString(R.string.fill_all_fields));
        }
    }

    private void showError(String message) {
        Snackbar.make(viewFlipper, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onReady() {
        emailInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
    }

    @Override
    public void onProgress() {
        emailInput.setEnabled(false);
        viewFlipper.setDisplayedChild(1);
    }

    @Override
    public void onAbuseSent() {
        Toast.makeText(this, R.string.thanks_for_attention, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onError() {
        emailInput.setEnabled(true);
        viewFlipper.setDisplayedChild(0);
        showError(getString(R.string.unable_to_send_abuse));
    }
}
