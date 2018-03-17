package com.tomclaw.appsend.net;

import android.content.ContentResolver;
import android.content.Context;

import com.tomclaw.appsend.AppSend;
import com.tomclaw.appsend.core.ContentResolverLayer;
import com.tomclaw.appsend.main.controller.DiscussController;
import com.tomclaw.appsend.net.request.FetchRequest;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.Logger;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * Created by solkin on 22/04/16.
 */
@EBean(scope = EBean.Scope.Singleton)
public class Session {

    @RootContext
    Context context;

    private UserHolder userHolder;

    public static Session getInstance() {
        return Session_.getInstance_(AppSend.app());
    }

    @AfterInject
    void init() {
        if (userHolder == null) {
            userHolder = UserHolder.create(context);
        }
    }

    public void start() {
        final ContentResolver contentResolver = context.getContentResolver();
        if (getUserData().isRegistered()) {
            DiscussController.getInstance().onUserReady();
            Logger.log("start events fetching with guid: " + getUserData().getGuid());
            new Thread() {
                public void run() {
                    Logger.log("fetch started");
                    FetchRequest request = new FetchRequest(getUserData().getFetchTime());
                    do {
                        int requestResult = request.onRequest(contentResolver, userHolder);
                        Logger.log("fetch result is " + requestResult);
                        if (requestResult != Request.REQUEST_DELETE) {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ignored) {
                            }
                        }
                        request.setTime(getUserData().getFetchTime());
                        Logger.log("fetch restart attempt");
                    } while (getUserData().isRegistered());
                    Logger.log("quit fetch loop");
                }
            }.start();
        } else {
            Logger.log("user needs to be registered");
            RequestHelper.requestUserRegistration(ContentResolverLayer.from(contentResolver));
        }
    }

    public UserData getUserData() {
        return userHolder.getUserData();
    }
}
