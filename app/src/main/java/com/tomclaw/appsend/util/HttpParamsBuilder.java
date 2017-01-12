package com.tomclaw.appsend.util;

import android.util.Pair;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Solkin on 28.09.2014.
 */
public class HttpParamsBuilder extends ArrayList<Pair<String, String>> {

    public HttpParamsBuilder appendParam(String key, String value) {
        add(new Pair<>(key, value));
        return this;
    }

    public void sortParams() {
        Collections.sort(this, new Comparator<Pair<String, String>>() {
            @Override
            public int compare(Pair<String, String> lhs, Pair<String, String> rhs) {
                return lhs.first.compareTo(rhs.first);
            }
        });
    }

    public String build() throws UnsupportedEncodingException {
        return HttpUtil.prepareParameters(this);
    }

    public void reset() {
        clear();
    }
}
