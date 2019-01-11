package com.tomclaw.appsend.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * Created by solkin on 13.07.15.
 */
public class BubbleColorDrawable extends Drawable {

    private static int OFFSET;
    private static int BUBBLE_RADIUS;
    private final int color;
    private final Corner corner;
    private Paint whitePaint;

    public BubbleColorDrawable(Context context, int color, Corner corner) {
        this.color = color;
        this.corner = corner;
        OFFSET = dp(8, context);
        BUBBLE_RADIUS = dp(6, context);
        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect r = getBounds();
        RectF rect = new RectF(r);
        rect.inset(OFFSET, OFFSET);
        Path path = new Path();
        if (corner == Corner.LEFT) {
            path.moveTo(0, 0);
            path.lineTo(r.width() - OFFSET, 0);
            path.arcTo(new RectF(r.right - BUBBLE_RADIUS * 2, 0, r.right, BUBBLE_RADIUS * 2), 270, 90);
            path.lineTo(r.width(), r.height() - OFFSET);
            path.arcTo(new RectF(r.right - BUBBLE_RADIUS * 2, r.bottom - BUBBLE_RADIUS * 2, r.right, r.bottom), 0, 90);
            path.lineTo(BUBBLE_RADIUS, r.height());
            path.arcTo(new RectF(OFFSET, r.bottom - BUBBLE_RADIUS * 2, BUBBLE_RADIUS * 2 + OFFSET, r.bottom), 90, 90);
            path.lineTo(OFFSET, OFFSET);
        } else if (corner == Corner.RIGHT) {
            path.moveTo(BUBBLE_RADIUS, 0);
            path.lineTo(r.width(), 0);
            path.lineTo(r.width() - OFFSET, OFFSET);
            path.lineTo(r.width() - OFFSET, r.height() - BUBBLE_RADIUS);
            path.arcTo(new RectF(r.right - BUBBLE_RADIUS * 2 - OFFSET, r.bottom - BUBBLE_RADIUS * 2, r.right - OFFSET, r.bottom), 0, 90);
            path.lineTo(BUBBLE_RADIUS, r.height());
            path.arcTo(new RectF(0, r.bottom - BUBBLE_RADIUS * 2, BUBBLE_RADIUS * 2, r.bottom), 90, 90);
            path.lineTo(0, BUBBLE_RADIUS);
            path.arcTo(new RectF(0, 0, BUBBLE_RADIUS * 2, BUBBLE_RADIUS * 2), 180, 90);
        } else if (corner == Corner.NONE) {
            rect.inset(-OFFSET, -OFFSET);
            path.addRoundRect(rect, BUBBLE_RADIUS, BUBBLE_RADIUS, Path.Direction.CW);
        }
        path.close();
        whitePaint.setColor(color);
        whitePaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, whitePaint);
    }

    private int dp(float v, Context context) {
        return (int) (v * context.getResources().getDisplayMetrics().density + 0.5);
    }
}
