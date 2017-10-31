package com.tomclaw.appsend.main.ratings;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.widget.RatingBar;

import com.tomclaw.appsend.R;

import static com.tomclaw.appsend.util.ColorHelper.getAttributedColor;

/**
 * Created by Igor on 23.10.2017.
 */
public class RatingsHelper {

    public static void tintRatingIndicator(Context context, RatingBar ratingIndicator) {
        int emptyColor = getAttributedColor(context, R.attr.rating_empty);
        int fillColor = getAttributedColor(context, R.attr.rating_fill);
        tintRatingIndicator(ratingIndicator, emptyColor, fillColor);
    }

    public static void tintRatingIndicator(RatingBar ratingIndicator, int emptyColor, int fillColor) {
        LayerDrawable stars = (LayerDrawable) ratingIndicator.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(emptyColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(emptyColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(fillColor, PorterDuff.Mode.SRC_ATOP);
    }
}
