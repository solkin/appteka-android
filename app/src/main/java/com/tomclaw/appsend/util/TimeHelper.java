package com.tomclaw.appsend.util;

import android.content.Context;
import android.text.format.DateFormat;

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
    private static final SimpleDateFormat simpleTimeFormat12;
    private static final SimpleDateFormat simpleTimeFormat24;

    private final SimpleDateFormat timeFormat;

    private static TimeHelper instance;

    static {
        Locale locale = Locale.getDefault();
        simpleDateFormat = new SimpleDateFormat("dd.MM.yy", locale);
        simpleTimeFormat12 = new SimpleDateFormat("h:mm a", locale);
        simpleTimeFormat24 = new SimpleDateFormat("HH:mm", locale);
    }

    public static TimeHelper timeHelper() {
        if (instance == null) {
            throw new IllegalStateException("TimeHelper must be initialized first");
        }
        return instance;
    }

    private TimeHelper(Context context) {
        timeFormat = DateFormat.is24HourFormat(context) ? simpleTimeFormat24 : simpleTimeFormat12;
    }

    public static void init(Context context) {
        instance = new TimeHelper(context);
    }

    private SimpleDateFormat getTimeFormat() {
        return timeFormat;
    }

    public String getFormattedDate(long timestamp) {
        return simpleDateFormat.format(timestamp);
    }

    public String getFormattedTime(long timestamp) {
        return getTimeFormat().format(timestamp);
    }
}
