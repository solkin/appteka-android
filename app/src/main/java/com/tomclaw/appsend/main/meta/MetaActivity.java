package com.tomclaw.appsend.main.meta;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.StoreServiceHolder;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;

/**
 * Created by solkin on 23.09.17.
 */
@EActivity(R.layout.meta_activity)
public class MetaActivity extends AppCompatActivity {

    @Bean
    StoreServiceHolder serviceHolder;

    @InstanceState
    Bundle bundle = new Bundle();

    @AfterViews
    void init() {
        if (!bundle.isEmpty()) {
            Log.d("~@~", "Bundle exist");
            Log.d("~@~", "key = " + bundle.getString("key"));
        } else {
            Log.d("~@~", "Bundle do not exist");
            bundle.putString("key", "value");
        }
    }
}
