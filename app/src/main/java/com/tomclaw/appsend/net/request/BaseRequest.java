package com.tomclaw.appsend.net.request;

import com.tomclaw.appsend.util.HttpParamsBuilder;
import com.tomclaw.appsend.util.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Igor on 07.07.2015.
 */
public abstract class BaseRequest extends JsonRequest {

    public static final int PROTOCOL_VERSION = 1;
    public static final String BASE_URL = "http://appsend.store/api/";

    protected static final int STATUS_PROTOCOL_OUTDATED = 417;
    protected static final int STATUS_SERVER_INTERNAL_ERROR = 500;
    protected static final int STATUS_OK = 200;
    protected static final int STATUS_INVALID_DATA = 400;

    @Override
    protected String getHttpRequestType() {
        return HttpUtil.GET;
    }

    @Override
    protected String getUrl() {
        return BASE_URL + getApiName() + ".php";
    }

    protected abstract String getApiName();

    @Override
    protected final HttpParamsBuilder getParams() {
        String guid = getUserHolder().getUserData().getGuid();
        HttpParamsBuilder builder = new HttpParamsBuilder();
        builder.appendParam("v", PROTOCOL_VERSION);
        if (isUserBased()) {
            builder.appendParam("guid", guid);
        }
        appendParams(builder);
        return builder;
    }

    protected abstract void appendParams(HttpParamsBuilder builder);

    @Override
    protected final int parseJson(JSONObject response) throws JSONException {
        int status = response.getInt("status");
        switch (status) {
            case STATUS_PROTOCOL_OUTDATED: {
                return REQUEST_PENDING;
            }
            case STATUS_SERVER_INTERNAL_ERROR: {
                return REQUEST_PENDING;
            }
        }
        return parsePacket(status, response);
    }

    protected abstract int parsePacket(int status, JSONObject object) throws JSONException;
}
