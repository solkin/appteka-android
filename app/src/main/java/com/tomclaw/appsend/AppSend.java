package com.tomclaw.appsend;

import android.app.Application;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.net.RequestDispatcher;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.MemberImageHelper;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.TimeHelper;
import com.tomclaw.appsend.util.states.StateHolder;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import static com.tomclaw.appsend.util.ManifestHelper.getManifestString;

/**
 * Created by ivsolkin on 21.03.17.
 */
@EApplication
public class AppSend extends Application {

    private static final String FLURRY_IDENTIFIER_KEY = "com.yahoo.flurry.appIdentifier";
    private static final String APP_SESSION = StringUtil.generateRandomString(32);

    private static AppSend app;

    @Bean
    Session session;

    @AfterInject
    void init() {
        app = this;
        session.init();
        String flurryIdentifier = getManifestString(this, FLURRY_IDENTIFIER_KEY);
        FlurryAgent.init(this, flurryIdentifier);
        TimeHelper.init(this);
        StateHolder.init();
        MemberImageHelper.init(this);
        DiscussController.getInstance();
        RequestDispatcher
                .init(this, session.getUserHolder(), APP_SESSION, Request.REQUEST_TYPE_SHORT)
                .startObservation();
    }

    public static AppSend app() {
        return app;
    }

}
