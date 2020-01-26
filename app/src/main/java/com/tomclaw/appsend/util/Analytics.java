package com.tomclaw.appsend.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.tomclaw.appsend.core.Task;
import com.tomclaw.appsend.core.TaskExecutor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.tomclaw.appsend.AppSend.app;
import static com.tomclaw.appsend.util.StreamHelper.safeClose;

public class Analytics {

    private static final String API_URL = "https://zibuhoker.ru/api/track.php";
    private static final int BATCH_SIZE = 20;

    private Executor executor = Executors.newSingleThreadExecutor();

    private static class Holder {
        static Analytics instance = new Analytics().createAnalytics();
    }

    public static Analytics getInstance() {
        return Holder.instance;
    }

    private String uniqueID;

    private Analytics() {
    }

    public static void trackEvent(final String event) {
        trackEvent(event, false);
    }

    public static void trackEvent(final String event, boolean isImmediate) {
        getInstance().trackEventInternal(event, isImmediate);
    }

    public static void uploadEvents() {
        getInstance().uploadEventsInternal();
    }

    private void trackEventInternal(final String event, final boolean isImmediate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (isImmediate) {
                    trackEventInternalImmediate(event);
                } else {
                    writeEvent(event);
                    uploadEventsInternal();
                }
            }
        });
    }

    private void uploadEventsInternal() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendEvents();
            }
        });
    }

    private void trackEventInternalImmediate(final String event) {
        TaskExecutor.getInstance().execute(new Task() {
            @Override
            public void executeBackground() {
                sendEvent(createEvent(event), createInfo());
            }
        });
    }

    private AnalyticsEvent createEvent(String event) {
        long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        return createEvent(event, time);
    }

    private EnvironmentInfo createInfo() {
        String packageName = app().getPackageName();
        int versionCode = 0;
        PackageManager manager = app().getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        String deviceName = Build.MANUFACTURER + " " + Build.MODEL;
        return new EnvironmentInfo(
                packageName,
                versionCode,
                Build.VERSION.SDK_INT,
                uniqueID,
                deviceName
        );
    }

    private static AnalyticsEvent createEvent(String event, long time) {
        return new AnalyticsEvent(
                time,
                event
        );
    }

    private void writeEvent(String event) {
        long time = System.currentTimeMillis();
        DataOutputStream output = null;
        File file = new File(eventsDir(), generateEventFileName(event, time));
        try {
            byte version = 1;
            output = new DataOutputStream(new FileOutputStream(file));
            output.writeByte(version);
            output.writeLong(TimeUnit.MILLISECONDS.toSeconds(time));
            output.writeUTF(event);
            output.flush();
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        } finally {
            safeClose(output);
        }
    }

    @Nullable
    private AnalyticsEvent readEvent(File file) {
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(file));
            byte version = input.readByte();
            if (version == 1) {
                long time = input.readLong();
                String event = input.readUTF();
                return createEvent(event, time);
            }
        } catch (IOException ignored) {
        } finally {
            safeClose(input);
        }
        return null;
    }

    private void sendEvents() {
        File dir = eventsDir();
        List<File> files = new ArrayList<>(Arrays.asList(dir.listFiles()));
        if (files.size() >= BATCH_SIZE) {
            EnvironmentInfo info = createInfo();
            Collections.sort(files, new AnalyticsFileComparator());
            List<AnalyticsEvent> sendEvents = new ArrayList<>();
            List<File> filesToRemove = new ArrayList<>();
            do {
                File file = files.remove(0);
                AnalyticsEvent event = readEvent(file);
                if (event != null) {
                    sendEvents.add(event);
                }
                filesToRemove.add(file);
                if (sendEvents.size() >= BATCH_SIZE) {
                    EventsBatch batch = new EventsBatch(info, sendEvents);
                    String data = GsonSingleton.getInstance().toJson(batch);
                    Logger.log("[analytics] batch data: " + data);
                    try {
                        String result = HttpUtil.executePost(API_URL, data);
                        Logger.log("[analytics] batch result: " + result);
                    } catch (IOException ex) {
                        Logger.log("[analytics] error sending analytics track", ex);
                        return;
                    }
                    for (File f : filesToRemove) {
                        //noinspection ResultOfMethodCallIgnored
                        f.delete();
                        Logger.log("[analytics] remove event file: " + f.getName());
                    }
                    sendEvents.clear();
                    filesToRemove.clear();
                }
            } while ((files.size() + filesToRemove.size()) >= BATCH_SIZE);
        }
    }

    private void sendEvent(AnalyticsEvent event, EnvironmentInfo info) {
        HttpParamsBuilder params = new HttpParamsBuilder()
                .appendParam("app_id", info.appId)
                .appendParam("app_version", info.appVersion)
                .appendParam("os_version", info.osVersion)
                .appendParam("device_id", info.deviceId)
                .appendParam("device_name", info.deviceName)
                .appendParam("time", event.time)
                .appendParam("event", event.event);
        try {
            String result = HttpUtil.executePost(API_URL, params);
            Logger.log("[analytics] result: " + result);
        } catch (IOException ex) {
            Logger.log("[analytics] error sending analytics track", ex);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File eventsDir() {
        File dir = new File(app().getFilesDir(), "analytics_events");
        dir.mkdirs();
        return dir;
    }

    private File analyticsFile() {
        return new File(app().getFilesDir(), "analytics.uuid");
    }

    private Analytics createAnalytics() {
        String uuid = null;
        DataInputStream is = null;
        try {
            File file = analyticsFile();
            if (file.exists()) {
                is = new DataInputStream(new FileInputStream(file));
                uuid = is.readUTF();
            }
        } catch (IOException ignored) {
        } finally {
            safeClose(is);
        }
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        if (!uuid.equals(uniqueID)) {
            DataOutputStream os = null;
            try {
                File file = analyticsFile();
                os = new DataOutputStream(new FileOutputStream(file));
                os.writeUTF(uuid);
            } catch (IOException ignored) {
            } finally {
                safeClose(os);
            }
        }
        uniqueID = uuid;
        return this;
    }

    private static String generateEventFileName(String event, long time) {
        return time + "-" + md5(event) + ".event";
    }

    private static long getFileNameTime(String fileName) {
        int timeDivider = fileName.indexOf('-');
        if (timeDivider > 0) {
            return Long.parseLong(fileName.substring(0, timeDivider));
        }
        return 0;
    }

    private static String md5(final String s) {
        final String MD5 = "MD5";
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2) {
                    h.insert(0, "0");
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException ignored) {
        }
        return "";
    }

    private static class AnalyticsFileComparator implements Comparator<File> {

        @Override
        public int compare(File o1, File o2) {
            return compare(getFileNameTime(o1.getName()), getFileNameTime(o2.getName()));
        }

        @SuppressWarnings("UseCompareMethod")
        private static int compare(long x, long y) {
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }

    }

    private static class EventsBatch {

        @SerializedName("info")
        final EnvironmentInfo info;

        @SerializedName("events")
        final List<AnalyticsEvent> events;

        EventsBatch(EnvironmentInfo info, List<AnalyticsEvent> events) {
            this.info = info;
            this.events = events;
        }

    }

    private static class EnvironmentInfo {

        @SerializedName("app_id")
        final String appId;

        @SerializedName("app_version")
        final int appVersion;

        @SerializedName("os_version")
        final int osVersion;

        @SerializedName("device_id")
        final String deviceId;

        @SerializedName("device_name")
        final String deviceName;

        EnvironmentInfo(String appId, int appVersion, int osVersion,
                               String deviceId, String deviceName) {
            this.appId = appId;
            this.appVersion = appVersion;
            this.osVersion = osVersion;
            this.deviceId = deviceId;
            this.deviceName = deviceName;
        }

    }

    private static class AnalyticsEvent {

        @SerializedName("time")
        final long time;

        @SerializedName("event")
        final String event;

        AnalyticsEvent(long time, String event) {
            this.time = time;
            this.event = event;
        }

    }

}
