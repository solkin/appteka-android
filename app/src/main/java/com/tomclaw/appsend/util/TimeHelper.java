package com.tomclaw.appsend.util;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by solkin on 05/05/14.
 */
public class TimeHelper {

    /**
     * Date and time format helpers
     */
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");
    private static final SimpleDateFormat simpleTimeFormat12 = new SimpleDateFormat("h:mm a");
    private static final SimpleDateFormat simpleTimeFormat24 = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat simpleTimeFormatSeconds = new SimpleDateFormat("mm:ss");

    private final SimpleDateFormat timeFormat;

    private static TimeHelper instance;

    public static TimeHelper timeHelper() {
        if (instance == null) throw new IllegalStateException("TimeHelper must be initialized first");
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

    public static int getYears(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() - timeStamp);
        return calendar.get(Calendar.YEAR) - 1970;
    }

    public static Calendar clearTimes(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c;
    }

    public String getTime(long timestamp) {
        return simpleTimeFormatSeconds.format(timestamp);
    }
}
