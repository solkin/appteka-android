package com.tomclaw.appsend;

import android.app.Application;

import com.esotericsoftware.kryo.Kryo;
import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.main.local.ApkItemsState;
import com.tomclaw.appsend.main.local.AppItemsState;
import com.tomclaw.appsend.main.store.StoreItemsState;
import com.tomclaw.appsend.net.RequestDispatcher;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.MemberImageHelper;
import com.tomclaw.appsend.util.states.StateHolder;
import com.tomclaw.appsend.util.StringUtil;
import com.tomclaw.appsend.util.TimeHelper;

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

    private static Kryo kryo;

    @Bean
    Session session;

    @AfterInject
    void init() {
        app = this;
        initKryo();
        session.init();
        String flurryIdentifier = getManifestString(this, FLURRY_IDENTIFIER_KEY);
        FlurryAgent.init(this, flurryIdentifier);
        TimeHelper.init(this);
        StateHolder.init(this);
        MemberImageHelper.init(this);
        DiscussController.getInstance();
        RequestDispatcher
                .init(this, session.getUserHolder(), APP_SESSION, Request.REQUEST_TYPE_SHORT)
                .startObservation();
        session.start();
    }

    public static AppSend app() {
        return app;
    }

    public static Kryo kryo() {
        return kryo;
    }

    private void initKryo() {
        kryo = new Kryo();
        kryo.register(StoreItemsState.class);
        kryo.register(AppItemsState.class);
        kryo.register(ApkItemsState.class);
    }

}
