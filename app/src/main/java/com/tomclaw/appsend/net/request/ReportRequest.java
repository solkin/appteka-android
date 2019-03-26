package com.tomclaw.appsend.net.request;

import com.tomclaw.appsend.util.HttpParamsBuilder;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by solkin on 23/04/16.
 */
public class ReportRequest extends BaseRequest {

    private long msgId;

    public ReportRequest() {
    }

    public ReportRequest(long msgId) {
        this.msgId = msgId;
    }

    @Override
    protected String getApiName() {
        return "chat/report";
    }

    @Override
    protected void appendParams(HttpParamsBuilder builder) {
        builder.appendParam("msg_id", msgId);
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
