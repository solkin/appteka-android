package com.tomclaw.appsend.main.migrate;

import androidx.appcompat.app.AppCompatActivity;

import com.tomclaw.appsend.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;

/**
 * Created by Solkin on 17.12.2014.
 */
@EActivity(R.layout.migrate_activity)
public class MigrateActivity extends AppCompatActivity {

    @Click(R.id.close_button)
    void onClose() {
        finish();
    }

}
