package com.tomclaw.appsend.main.local;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by solkin on 05/05/14.
 */
public class TimeHelper {

    /**
     * Date and time format helpers
     */
    private static final SimpleDateFormat simpleDateFormat;

    private static TimeHelper instance;

    static {
        Locale locale = Locale.getDefault();
        simpleDateFormat = new SimpleDateFormat("dd.MM.yy", locale);
    }

    public static TimeHelper timeHelper() {
        if (instance == null) {
            throw new IllegalStateException("TimeHelper must be initialized first");
        }
        return instance;
    }

    private TimeHelper() {
    }

    public static void init() {
        instance = new TimeHelper();
    }

    public String getFormattedDate(long timestamp) {
        return simpleDateFormat.format(timestamp);
    }

}
