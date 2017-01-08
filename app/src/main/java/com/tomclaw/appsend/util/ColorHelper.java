package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.TypedValue;

/**
 * Created by ivsolkin on 08.01.17.
 */

public class ColorHelper {

    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int darker(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    public static int getAttributedColor(Context context, int attr) {
        int[] set = {attr};
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, set);
        int color = a.getColor(0, Color.WHITE);
        a.recycle();
        return color;
    }
}
