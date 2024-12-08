package com.tomclaw.appsend.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 13.10.13
 * Time: 20:09
 */
public class StringUtil {

    public static Random random;

    static {
        random = new Random(System.currentTimeMillis());
    }

    public static String generateCookie() {
        return Long.toHexString(System.currentTimeMillis()) + "-" + generateRandomString(random, 16, 16);
    }

    public static String generateRandomString(Random r, int minChars, int maxChars) {
        int wordLength = minChars;
        int delta = maxChars - minChars;
        if (delta > 0) {
            wordLength += r.nextInt(delta);
        }
        StringBuilder sb = new StringBuilder(wordLength);
        for (int i = 0; i < wordLength; i++) { // For each letter in the word
            char tmp = (char) ('a' + r.nextInt('z' - 'a')); // Generate a letter between a and z
            sb.append(tmp); // Add it to the String
        }
        return sb.toString();
    }

}
