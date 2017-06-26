package com.tomclaw.appsend.net.request;

import android.content.ContentResolver;

import com.tomclaw.appsend.net.UserHolder;
import com.tomclaw.appsend.util.Unobfuscatable;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 6/9/13
 * Time: 7:25 PM
 */
public abstract class Request implements Unobfuscatable {

    /**
     * Request types
     */
    public static final int REQUEST_TYPE_SHORT = 0x00;
    public static final int REQUEST_TYPE_DOWNLOAD = 0x01;
    public static final int REQUEST_TYPE_UPLOAD = 0x02;

    /**
     * Request state flags
     */
    public static final int REQUEST_PENDING = 0x00;
    public static final int REQUEST_SENT = 0x01;
    public static final int REQUEST_LATER = 0x02;
    public static final int REQUEST_DELETE = 0xff;

    private ContentResolver contentResolver;
    private transient UserHolder userHolder;

    public ContentResolver getContentResolver() {
        return contentResolver;
    }

    public UserHolder getUserHolder() {
        return userHolder;
    }

    /**
     * Builds outgoing request and sends it over the network.
     *
     * @return int - status we must setup to this request
     */
    public final int onRequest(ContentResolver contentResolver, UserHolder userHolder) {
        this.contentResolver = contentResolver;
        this.userHolder = userHolder;
        return executeRequest();
    }

    public abstract int executeRequest();

    public abstract boolean isUserBased();

    public static abstract class RequestException extends Exception {
    }

    public static class RequestPendingException extends RequestException {
    }

    public static class RequestLaterException extends RequestException {
    }

    public static class RequestDeleteException extends RequestException {
    }

    public static class RequestCancelledException extends RequestException {
    }
}
