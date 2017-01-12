package com.tomclaw.appsend.main.item;


/**
 * Created by ivsolkin on 09.01.17.
 */
public abstract class BaseItem {

    public static final int APP_ITEM = 0x00001;
    public static final int APK_ITEM = 0x00010;
    public static final int STORE_ITEM = 0x00100;
    public static final int DONATE_ITEM = 0x01000;
    public static final int COUCH_ITEM = 0x10000;

    public abstract int getType();
}
