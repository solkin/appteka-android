package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

/**
 * Created by ivsolkin on 08.01.17.
 */

public class ColorHelper {

    public static int getAttributedColor(Context context, int attr) {
        int[] set = {attr};
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, set);
        int color = a.getColor(0, Color.WHITE);
        a.recycle();
        return color;
    }
}
