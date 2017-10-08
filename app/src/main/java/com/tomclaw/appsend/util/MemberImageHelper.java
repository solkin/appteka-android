package com.tomclaw.appsend.util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import com.tomclaw.appsend.R;

import java.util.Random;

/**
 * Created by Solkin on 11.09.2015.
 */
public class MemberImageHelper {

    private int defaultColor;
    private int[] colors;
    private TypedArray avatars;

    private Random random = new Random();

    private static MemberImageHelper instance;

    public static MemberImageHelper memberImageHelper() {
        if (instance == null)
            throw new IllegalStateException("MemberImageHelper must be initialized first");
        return instance;
    }

    private MemberImageHelper(int defaultColor, int[] colors, TypedArray avatars) {
        this.defaultColor = defaultColor;
        this.colors = colors;
        this.avatars = avatars;
    }

    public static void init(Context context) {
        Resources resources = context.getResources();
        instance = new MemberImageHelper(
                resources.getColor(R.color.primary_color),
                resources.getIntArray(R.array.palette),
                resources.obtainTypedArray(R.array.glyph_icons)
        );
    }

    public int getColor(long userId) {
        if (userId == 0) {
            return defaultColor;
        }
        random.setSeed(hash(userId, 1000));
        return colors[random.nextInt(colors.length)];
    }

    public int getAvatar(long userId, boolean isThreadOwner) {
        random.setSeed(hash(userId, 0));
        int avatar;
        if (isThreadOwner) {
            avatar = R.drawable.crown;
        } else {
            avatar = avatars.getResourceId(random.nextInt(avatars.length()), 0);
        }
        return avatar;
    }

    public long hash(long value, int seed) {
        String str = String.valueOf(value);
        long hash = seed;
        for (int i = 0; i < str.length(); i++) {
            hash = str.charAt(i) + ((hash << 5) - hash);
        }
        return hash;
    }
}
