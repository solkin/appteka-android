package com.tomclaw.appsend;


/**
 * Created by ivsolkin on 09.01.17.
 */
public abstract class BaseItem {

    public static final int APP_ITEM = 0x0001;
    public static final int APK_ITEM = 0x0010;
    public static final int DONATE_ITEM = 0x0100;
    public static final int COUCH_ITEM = 0x1000;

    public abstract int getType();
}
