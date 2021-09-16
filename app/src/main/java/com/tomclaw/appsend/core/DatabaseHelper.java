package com.tomclaw.appsend.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.tomclaw.appsend.util.LegacyLogger;
import com.tomclaw.appsend.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.tomclaw.appsend.util.StringUtil.generateRandomText;

/**
 * Created with IntelliJ IDEA.
 * User: solkin
 * Date: 4/23/13
 * Time: 10:55 AM
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private Random random;
    private int msgId;
    private long time;
    private SQLiteDatabase db;
    private boolean isMock = false;
    private boolean isDropTables = false;
    private boolean isExportDb = false;

    public DatabaseHelper(Context context) {
        super(context, Config.DB_NAME, null, Config.DB_VERSION);
        if (isMock && isDropTables) {
            onCreate(getWritableDatabase());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Drop tables on mock mode.
            if (isMock && isDropTables) {
                db.execSQL("DROP TABLE IF EXISTS " + GlobalProvider.REQUEST_TABLE);
                db.execSQL("DROP TABLE IF EXISTS " + GlobalProvider.MESSAGES_TABLE);
            }

            // Creating roster database.
            db.execSQL(GlobalProvider.DB_CREATE_REQUEST_TABLE_SCRIPT);
            db.execSQL(GlobalProvider.DB_CREATE_MESSAGES_TABLE_SCRIPT);

            if (isMock) {
                mockDb(db);
            }

            LegacyLogger.log("DB created: " + db.toString());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void mockDb(SQLiteDatabase db) {
        db.beginTransaction();
        this.db = db;
        random = new Random(System.currentTimeMillis());
        msgId = 1;
        time = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        insertThread();

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertThread() {
        int usersCount = random.nextInt(100) + 100;
        int messagesCount = random.nextInt(200) + 200;

        long userId = 0;

        ContentValues messageValues = new ContentValues();

        for (int c = 1; c < messagesCount; c++) {
            int msgUserId = random.nextInt(usersCount);
            boolean self = msgUserId == userId;
            int wordCount = 1 + random.nextInt(10);
            String text = generateRandomText(random, wordCount);
            String cookie = StringUtil.generateRandomString(random, 20, 20);
            long delay = TimeUnit.MINUTES.toMillis(random.nextInt(24) + 1);
            boolean service = random.nextInt(15) == 5;
            if (service) {
                int type = GlobalProvider.MESSAGE_TYPE_JOINED;

                messageValues.put(GlobalProvider.MESSAGES_USER_ID, msgUserId);
                messageValues.put(GlobalProvider.MESSAGES_PREV_MSG_ID, msgId);
                messageValues.put(GlobalProvider.MESSAGES_MSG_ID, ++msgId);
                messageValues.put(GlobalProvider.MESSAGES_TEXT, "");
                messageValues.put(GlobalProvider.MESSAGES_TIME, time += delay);
                messageValues.put(GlobalProvider.MESSAGES_COOKIE, cookie);
                messageValues.put(GlobalProvider.MESSAGES_TYPE, type);
                messageValues.put(GlobalProvider.MESSAGES_DIRECTION, GlobalProvider.DIRECTION_SERVICE);

                db.insert(GlobalProvider.MESSAGES_TABLE, null, messageValues);
            } else {
                messageValues.put(GlobalProvider.MESSAGES_USER_ID, msgUserId);
                messageValues.put(GlobalProvider.MESSAGES_PREV_MSG_ID, msgId);
                messageValues.put(GlobalProvider.MESSAGES_MSG_ID, ++msgId);
                messageValues.put(GlobalProvider.MESSAGES_TEXT, text);
                messageValues.put(GlobalProvider.MESSAGES_TIME, time += delay);
                messageValues.put(GlobalProvider.MESSAGES_COOKIE, cookie);
                messageValues.put(GlobalProvider.MESSAGES_TYPE, GlobalProvider.MESSAGE_TYPE_PLAIN);
                messageValues.put(GlobalProvider.MESSAGES_DIRECTION, self ? GlobalProvider.DIRECTION_OUTGOING : GlobalProvider.DIRECTION_INCOMING);

                db.insert(GlobalProvider.MESSAGES_TABLE, null, messageValues);
            }
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (isExportDb) {
            exportDb(db);
        }
    }

    private void exportDb(SQLiteDatabase db) {
        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        FileChannel source;
        FileChannel destination;
        String currentDBPath = db.getPath();
        String backupDBPath = Config.DB_NAME + ".db";
        File currentDB = new File(currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Yo!
        LegacyLogger.log("Now we need to upgrade database from " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 1: {
                /*db.execSQL("ALTER TABLE " + GlobalProvider.ROSTER_BUDDY_TABLE
                        + " ADD COLUMN " + GlobalProvider.ROSTER_BUDDY_DRAFT + " text");*/
            }
        }
        LegacyLogger.log("Database upgrade completed");
    }
}
