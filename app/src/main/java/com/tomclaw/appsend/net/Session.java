package com.tomclaw.appsend.net;

import android.content.ContentResolver;

import com.tomclaw.appsend.core.ContentResolverLayer;
import com.tomclaw.appsend.net.request.FetchRequest;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.Logger;

/**
 * Created by solkin on 22/04/16.
 */
public class Session {

    private ContentResolver contentResolver;
    private UserHolder userHolder;

    private static Session instance;

    public static Session init(ContentResolver contentResolver, UserHolder userHolder) {
        instance = new Session(contentResolver, userHolder);
        return instance;
    }

    public static Session getInstance() {
        return instance;
    }

    public Session(ContentResolver contentResolver, UserHolder userHolder) {
        this.contentResolver = contentResolver;
        this.userHolder = userHolder;
    }

    public void start() {
        if (getUserData().isRegistered()) {
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
