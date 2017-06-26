package com.tomclaw.appsend.net.request;

import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by solkin on 23/04/16.
 */
public class PushRequest extends BaseRequest {

    public static final long NO_THREAD_ID = 0;
    public static final long NO_FILE_ID = 0;

    private String cookie;
    private long threadId;
    private long fileId;
    private String text;

    public PushRequest() {
    }

    public PushRequest(String cookie, long fileId, String text) {
        this.cookie = cookie;
        this.threadId = 0;
        this.fileId = fileId;
        this.text = text;
    }

    public PushRequest(String cookie, long threadId, long fileId, String text) {
        this.cookie = cookie;
        this.threadId = threadId;
        this.fileId = fileId;
        this.text = text;
    }

    @Override
    protected String getApiName() {
        return "push";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
        builder.appendParam("cookie", cookie);
        if (threadId != NO_THREAD_ID) {
            builder.appendParam("thread_id", threadId);
        }
        if (fileId != NO_FILE_ID) {
            builder.appendParam("file_id", fileId);
        }
        builder.appendParam("text", text);
    }

    @Override
    protected int parsePacket(int status, JSONObject object) throws JSONException {
        if (status == STATUS_OK) {
            return REQUEST_DELETE;
        }
        return REQUEST_PENDING;
    }

    @Override
    public boolean isUserBased() {
        return true;
    }
}
