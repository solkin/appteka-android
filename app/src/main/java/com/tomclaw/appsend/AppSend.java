package com.tomclaw.appsend;

import android.app.Application;

import com.flurry.android.FlurryAgent;

import static com.tomclaw.appsend.util.ManifestHelper.getManifestString;

/**
 * Created by ivsolkin on 21.03.17.
 */
public class AppSend extends Application {

    private static final String FLURRY_IDENTIFIER_KEY = "com.yahoo.flurry.appIdentifier";

    @Override
    public void onCreate() {
        super.onCreate();
        String flurryIdentifier = getManifestString(this, FLURRY_IDENTIFIER_KEY);
        FlurryAgent.init(this, flurryIdentifier);
    }
}
