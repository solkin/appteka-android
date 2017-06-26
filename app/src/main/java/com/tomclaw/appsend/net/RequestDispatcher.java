package com.tomclaw.appsend.net;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.text.TextUtils;

import com.tomclaw.appsend.core.Config;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.net.request.Request;
import com.tomclaw.appsend.util.GsonSingleton;
import com.tomclaw.appsend.util.Logger;
import com.tomclaw.appsend.util.QueryBuilder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 6/9/13
 * Time: 7:27 PM
 */
public class RequestDispatcher {

    private static final long PENDING_REQUEST_DELAY = 3000;
    private ContentResolver contentResolver;
    private final UserHolder userHolder;
    private final String appSession;
    /**
     * Variables
     */
    private int requestType;
    private DispatcherRunnable runnable;
    private ThreadPoolExecutor executor;
    private RequestObserver requestObserver;

    private volatile String executingRequestTag;

    private static RequestDispatcher instance;

    public static RequestDispatcher getInstance() {
        return instance;
    }

    public static RequestDispatcher init(Context context, UserHolder userHolder, String appSession,
                            int requestType) {
        instance = new RequestDispatcher(context.getContentResolver(), userHolder,
                appSession, requestType);
        return instance;
    }

    private RequestDispatcher(ContentResolver contentResolver, UserHolder userHolder,
                             String appSession, int requestType) {
        this.contentResolver = contentResolver;
        this.userHolder = userHolder;
        this.appSession = appSession;
        this.requestType = requestType;
        initExecutor();
        runnable = new DispatcherRunnable();
        requestObserver = new RequestObserver();
    }

    private void initExecutor() {
        executor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(3));
    }

    public void startObservation() {
        // Registering created observers.
        contentResolver.registerContentObserver(
                Config.REQUEST_RESOLVER_URI, true, requestObserver);
        // Almost done. Starting.
        notifyQueue();
    }

    /**
     * Stops task with specified tag.
     *
     * @param tag - tag of the task needs to be stopped.
     */
    public boolean stopRequest(String tag) {
        // First of all, check that task is executing or in queue.
        if (TextUtils.equals(tag, executingRequestTag)) {
            // Task is executing this moment.
            // Interrupt thread as faster as it can be!
            // Task will receive interrupt exception.
            executor.shutdownNow();
            initExecutor();
            notifyQueue();
            return true;
        } else {
            // Huh... Task is only in scheduled queue.
            // We can simply mark is as delayed "REQUEST_LATER".
            ContentValues contentValues = new ContentValues();
            contentValues.put(GlobalProvider.REQUEST_STATE, Request.REQUEST_LATER);
            contentResolver.update(Config.REQUEST_RESOLVER_URI, contentValues,
                    GlobalProvider.REQUEST_TAG + "='" + tag + "'", null);
            return false;
        }
    }

    private class DispatcherRunnable implements Runnable {

        @Override
        public void run() {
            QueryBuilder queryBuilder = new QueryBuilder();
            queryBuilder.columnEquals(GlobalProvider.REQUEST_TYPE, requestType).and()
                    .columnNotEquals(GlobalProvider.REQUEST_STATE, Request.REQUEST_LATER);
            // Registering created observers.
            Cursor requestCursor = queryBuilder.query(contentResolver, Config.REQUEST_RESOLVER_URI);
            // Check for we are ready to dispatch.
            if (requestCursor == null) {
                log("Something strange! Request or account cursor is null.");
                return;
            }
            try {
                dispatch(requestCursor);
            } finally {
                requestCursor.close();
            }
        }

        @SuppressWarnings("unchecked")
        private void dispatch(Cursor requestCursor) {
            // Yeah, we are ready.
            log("Dispatching requests.");
            int requests = 0;
            // Checking for at least one request in database.
            if (requestCursor.moveToFirst()) {
                requests = requestCursor.getCount();
                log("Found requests: " + requests);
                do {
                    log("Request...");
                    // Obtain necessary column index.
                    int rowColumnIndex = requestCursor.getColumnIndex(GlobalProvider.ROW_AUTO_ID);
                    int classColumnIndex = requestCursor.getColumnIndex(GlobalProvider.REQUEST_CLASS);
                    int sessionColumnIndex = requestCursor.getColumnIndex(GlobalProvider.REQUEST_SESSION);
                    int persistentColumnIndex = requestCursor.getColumnIndex(GlobalProvider.REQUEST_PERSISTENT);
                    int stateColumnIndex = requestCursor.getColumnIndex(GlobalProvider.REQUEST_STATE);
                    int bundleColumnIndex = requestCursor.getColumnIndex(GlobalProvider.REQUEST_BUNDLE);
                    int tagColumnIndex = requestCursor.getColumnIndex(GlobalProvider.REQUEST_TAG);
                    /*
                     * Если сессия совпадает, то постоянство задачи значения не имеет.
                     * Если задача непостоянная, сессия отличается, то задача отклоняется.
                     * Если задача постоянная, сессия отличается, то надо смотреть на статус:
                     *      В очереди:  задача отправляется, как и в случае "обработано".
                     *                  Обновляется ключ сессии приложения.
                     *      Обработано: задача может быть не доставленной до сервера, переотправить.
                     *                  Обновляется ключ сессии приложения.
                     *      Отправлено: задача была отправлена, не нуждается в отправке, хотя ответа явно
                     *                  не будет и задачу можно удалить.
                     */
                    // Obtain values.
                    int requestDbId = requestCursor.getInt(rowColumnIndex);
                    boolean isPersistent = requestCursor.getInt(persistentColumnIndex) == 1;
                    String requestAppSession = requestCursor.getString(sessionColumnIndex);
                    int requestState = requestCursor.getInt(stateColumnIndex);
                    // Checking for session is equals.
                    if (TextUtils.equals(requestAppSession, appSession)) {
                        if (requestState != Request.REQUEST_PENDING) {
                            log("Processed request of current session.");
                            requests--;
                            continue;
                        }
                        log("Normal request and will be processed now.");
                    } else {
                        boolean isDecline = false;
                        boolean isBreak = false;
                        // Checking for query is persistent.
                        if (isPersistent) {
                            switch (requestState) {
                                case Request.REQUEST_PENDING: {
                                    // Persistent request, might be processed at anytime.
                                    log("Persistent request, might be processed at anytime.");
                                    break;
                                }
                                case Request.REQUEST_SENT: {
                                    // Request sent, processed by server,
                                    // but we have no answer. Decline.
                                    log("Request sent, processed by server, " +
                                            "but we have no answer. Decline.");
                                    isDecline = true;
                                    break;
                                }
                            }
                        } else {
                            // Decline request.
                            isDecline = true;
                            log("Another session and not persistent request.");
                        }
                        // Checking for request is obsolete and must be declined.
                        if (isDecline) {
                            contentResolver.delete(Config.REQUEST_RESOLVER_URI,
                                    GlobalProvider.ROW_AUTO_ID + "='" + requestDbId + "'", null);
                            requests--;
                            break;
                        }
                    }

                    String requestClass = requestCursor.getString(classColumnIndex);
                    String requestBundle = requestCursor.getString(bundleColumnIndex);
                    String requestTag = requestCursor.getString(tagColumnIndex);

                    log("Request received: "
                            + "class = " + requestClass + "; "
                            + "session = " + requestAppSession + "; "
                            + "persistent = " + isPersistent + "; "
                            + "state = " + requestState + "; "
                            + "bundle = " + requestBundle + "");

                    int requestResult = Request.REQUEST_DELETE;
                    Request request = null;
                    try {
                        // Preparing request.
                        request = (Request) GsonSingleton.getInstance().fromJson(
                                requestBundle, Class.forName(requestClass));
                        // Checking for user registered.
                        if (request.isUserBased() && !userHolder.getUserData().isRegistered()) {
                            // Account is not available now. Let's send this request later.
                            requests--;
                            continue;
                        }
                        executingRequestTag = requestTag;
                        requestResult = request.onRequest(contentResolver, userHolder);
                    } catch (Throwable ex) {
                        log("Exception while loading request class: " + requestClass, ex);
                    } finally {
                        executingRequestTag = null;
                    }
                    // Checking for request result.
                    if (requestResult == Request.REQUEST_DELETE) {
                        // Result is delete-type.
                        log("Result is delete-type");
                        contentResolver.delete(Config.REQUEST_RESOLVER_URI,
                                GlobalProvider.ROW_AUTO_ID + "='" + requestDbId + "'", null);
                        requests--;
                    } else if (requestResult == Request.REQUEST_PENDING) {
                        // Request wasn't completed. We'll retry request a little bit later.
                        log("Request wasn't completed. We'll retry request a little bit later.");
                        break;
                    } else {
                        // Updating this request.
                        log("Updating this request");
                        String requestJson = GsonSingleton.getInstance().toJson(request);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(GlobalProvider.REQUEST_STATE, requestResult);
                        contentValues.put(GlobalProvider.REQUEST_BUNDLE, requestJson);
                        contentResolver.update(Config.REQUEST_RESOLVER_URI, contentValues,
                                GlobalProvider.ROW_AUTO_ID + "='" + requestDbId + "'", null);
                    }
                } while (requestCursor.moveToNext());
            }
            log("Dispatching completed, pending requests: " + requests);
            if (requests > 0) {
                // Pending guarantee dispatching after delay.
                log("Pending guarantee dispatching after delay");
                try {
                    Thread.sleep(PENDING_REQUEST_DELAY);
                } catch (InterruptedException ignored) {
                }
                notifyQueue();
            }
        }
    }

    private void log(String message) {
        Logger.log("rd[" + requestType + "]: " + message);
    }

    private void log(String message, Throwable exception) {
        Logger.log("rd[" + requestType + "]: " + message, exception);
    }

    public void notifyQueue() {
        try {
            executor.submit(runnable);
            log("Queue notification accepted.");
        } catch (RejectedExecutionException ignored) {
            // All right, this is useless task.
            log("Queue notification received, but we already have notification.");
        }
    }

    private class RequestObserver extends ContentObserver {

        /**
         * Creates a content observer.
         */
        public RequestObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            Logger.log("RequestDispatcher: onChange [selfChange = " + selfChange + "]");
            notifyQueue();
        }
    }
}
