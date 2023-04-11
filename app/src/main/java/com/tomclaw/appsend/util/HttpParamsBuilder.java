package com.tomclaw.appsend.util;

import android.util.Pair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by Solkin on 28.09.2014.
 */
public class HttpParamsBuilder extends ArrayList<Pair<String, String>> {

    public HttpParamsBuilder appendParam(String key, String value) {
        add(new Pair<>(key, value));
        return this;
    }

    public String build() throws UnsupportedEncodingException {
        return HttpUtil.prepareParameters(this);
    }
}
