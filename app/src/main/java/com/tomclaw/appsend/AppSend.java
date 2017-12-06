package com.tomclaw.appsend;

import android.app.Application;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.net.RequestDispatcher;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.UserHolder;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.MemberImageHelper;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.TimeHelper;

import static com.tomclaw.appsend.util.ManifestHelper.getManifestString;

/**
 * Created by ivsolkin on 21.03.17.
 */
public class AppSend extends Application {

    private static final String FLURRY_IDENTIFIER_KEY = "com.yahoo.flurry.appIdentifier";
    private static final String APP_SESSION = StringUtil.generateRandomString(32);

    private static AppSend app;

    @Override
    public void onCreate() {
        app = this;
        super.onCreate();
        String flurryIdentifier = getManifestString(this, FLURRY_IDENTIFIER_KEY);
        FlurryAgent.init(this, flurryIdentifier);
        TimeHelper.init(this);
        MemberImageHelper.init(this);
        DiscussController.getInstance().init(this);
        UserHolder userHolder = UserHolder.create(this);
        RequestDispatcher
                .init(this, userHolder, APP_SESSION, Request.REQUEST_TYPE_SHORT)
                .startObservation();
        Session
                .init(getContentResolver(), userHolder)
                .start();
    }

    public static AppSend app() {
        return app;
    }

}
