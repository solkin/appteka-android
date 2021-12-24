package com.tomclaw.appsend.net;

import android.content.ContentResolver;
import android.content.Context;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.Appteka;
import com.tomclaw.appsend.core.TaskExecutor;
import com.tomclaw.appsend.main.home.StatusCheckTask;
import com.tomclaw.appsend.main.home.UserRegisterTask;
import com.tomclaw.appsend.util.LegacyLogger;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by solkin on 22/04/16.
 */
@EBean(scope = EBean.Scope.Singleton)
public class Session {

    @RootContext
    Context context;

    private UserHolder userHolder;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> loopFuture = null;

    public static Session getInstance() {
        return Session_.getInstance_(Appteka.app());
    }

    public void init() {
        if (userHolder == null) {
            userHolder = UserHolder.create(context);
        }
        userHolder.attachListener(new UserDataListener() {
            @Override
            public void onUserDataChanged(@NonNull UserData userData) {
                stop();
                start();
            }
        });
    }

    public void start() {
        stop();
        final ContentResolver contentResolver = context.getContentResolver();
        if (getUserData().isRegistered()) {
//            DiscussController.getInstance().onUserReady();
            LegacyLogger.log("start events fetching with guid: " + getUserData().getGuid());
            Runnable runnable = new Runnable() {
                public void run() {
//                    LegacyLogger.log("fetch started");
//                    FetchRequest request = new FetchRequest(getUserData().getFetchTime());
//                    do {
//                        int requestResult = request.onRequest(contentResolver, userHolder);
//                        LegacyLogger.log("fetch result is " + requestResult);
//                        if (requestResult != Request.REQUEST_DELETE) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ignored) {
                            }
//                        }
//                        request.setTime(getUserData().getFetchTime());
//                        LegacyLogger.log("fetch restart attempt");
//                    } while (getUserData().isRegistered());
//                    LegacyLogger.log("quit fetch loop");
                }
            };
            loopFuture = executor.submit(runnable);
        } else {
            LegacyLogger.log("user needs to be registered");
            TaskExecutor.getInstance().execute(new UserRegisterTask());
//            RequestHelper.requestUserRegistration(ContentResolverLayer.from(contentResolver));
        }
    }

    public void stop() {
        if (loopFuture != null) {
            LegacyLogger.log("stop events fetching...");
            boolean result = loopFuture.cancel(true);
            LegacyLogger.log("events fetching " + (result ? "stopped" : "not stopped"));
            if (result) {
                loopFuture = null;
            }
        }
    }

    public UserData getUserData() {
        return userHolder.getUserData();
    }

    public UserHolder getUserHolder() {
        return userHolder;
    }
}
