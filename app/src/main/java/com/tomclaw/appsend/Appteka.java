package com.tomclaw.appsend;

import android.app.Application;

import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.net.RequestDispatcher;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.MemberImageHelper;
import com.tomclaw.appsend.util.PreferenceHelper;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.TimeHelper;
import com.tomclaw.appsend.util.states.StateHolder;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

/**
 * Created by ivsolkin on 21.03.17.
 */
@EApplication
public class Appteka extends Application {

    private static final String APP_SESSION = StringUtil.generateRandomString(32);

    private static Appteka app;

    private static boolean wasRegistered = false;
    private static int lastRunBuildNumber = 0;

    @Bean
    Session session;

    @AfterInject
    void init() {
        app = this;
        session.init();
        actuateFlags();
        TimeHelper.init(this);
        StateHolder.init();
        MemberImageHelper.init(this);
        DiscussController.getInstance();
        RequestDispatcher
                .init(this, session.getUserHolder(), APP_SESSION, Request.REQUEST_TYPE_SHORT)
                .startObservation();
    }

    public static Appteka app() {
        return app;
    }

    private void actuateFlags() {
        wasRegistered = session.getUserData().isRegistered();
        lastRunBuildNumber = PreferenceHelper.getLastRunBuildNumber(this);
        PreferenceHelper.updateLastRunBuildNumber(this);
    }

    public static boolean wasRegistered() {
        return wasRegistered;
    }

    public static int getLastRunBuildNumber() {
        return lastRunBuildNumber;
    }
}
