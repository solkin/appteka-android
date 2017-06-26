package com.tomclaw.appsend.net.request;

import com.tomclaw.appsend.util.HttpUtil;
import com.tomclaw.appsend.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by Igor on 07.07.2015.
 */
public abstract class JsonRequest extends HttpRequest {

    @Override
    protected int parseResponse(InputStream httpResponseStream) throws Throwable {
        String responseString = HttpUtil.streamToString(httpResponseStream);
        Logger.log("json response\n".concat(responseString));
        //MessagePack.newDefaultUnpacker(httpResponseStream).unpackValue().toJson();
        return parseJson(parseResponse(responseString));
    }

    protected JSONObject parseResponse(String responseString) throws JSONException {
        return new JSONObject(responseString);
    }

    protected abstract int parseJson(JSONObject response) throws JSONException;
}
