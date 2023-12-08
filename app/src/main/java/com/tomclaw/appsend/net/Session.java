package com.tomclaw.appsend.net;

import android.content.Context;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.util.LegacyLogger;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by solkin on 22/04/16.
 */
@EBean(scope = EBean.Scope.Singleton)
public class Session {

    @RootContext
    Context context;

    @Bean
    StoreServiceHolder serviceHolder;

    private UserHolder userHolder;

    public static Session getInstance() {
        return Session_.getInstance_(Appteka.app());
    }

    public void init() {
        if (userHolder == null) {
            userHolder = UserHolder.create(context);
        }
        userHolder.attachListener(userData -> {
            if (getUserData().isRegistered()) {
                userHolder.reloadProfile(serviceHolder);
            } else {
                LegacyLogger.log("user not registered");
            }
        });
    }

    public UserData getUserData() {
        return userHolder.getUserData();
    }

    public UserHolder getUserHolder() {
        return userHolder;
    }
}
