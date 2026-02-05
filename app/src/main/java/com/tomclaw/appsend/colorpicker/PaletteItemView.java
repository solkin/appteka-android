package com.tomclaw.appsend.colorpicker;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.color.utilities.Hct;
import com.tomclaw.appsend.R;

public class PaletteItemView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int seedColor = Color.GRAY;
    private boolean isChecked = false;
    private float checkScale = 0f;
    private Drawable checkDrawable;
    private float segmentGap = 0f;

    public PaletteItemView(Context context) {
        super(context);
        init(context, null);
    }

    public PaletteItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PaletteItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PaletteItemView);
            checkDrawable = a.getDrawable(R.styleable.PaletteItemView_checkIcon);
            segmentGap = a.getDimension(R.styleable.PaletteItemView_segmentGap, 0f);
            a.recycle();
        }
    }

    public void setSeedColor(int color) {
        this.seedColor = color;
        invalidate();
    }

    public void setChecked(boolean checked, boolean animate) {
        if (checked == isChecked || checkDrawable == null) return;
        isChecked = checked;

        if (animate) {
            float start = checked ? 0f : 1f;
            float end = checked ? 1f : 0f;
            ValueAnimator animator = ValueAnimator.ofFloat(start, end);
            animator.setDuration(200);
            animator.addUpdateListener(a -> {
                checkScale = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        } else {
            checkScale = checked ? 1f : 0f;
            invalidate();
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2f;

        @SuppressLint("DrawAllocation") Path clipPath = new Path();
        clipPath.addCircle(width / 2f, height / 2f, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);

        int primary = generateTone(seedColor, 40);
        int secondary = generateTone(seedColor, 70);
        int tertiary = generateTone(seedColor, 90);

        paint.setColor(primary);
        canvas.drawRect(0, 0, width, height / 2f - segmentGap / 2, paint);

        paint.setColor(secondary);
        canvas.drawRect(0, height / 2f + segmentGap / 2, width / 2f - segmentGap / 2, height, paint);

        paint.setColor(tertiary);
        canvas.drawRect(width / 2f + segmentGap / 2, height / 2f + segmentGap / 2, width, height, paint);

        if (checkScale > 0f && checkDrawable != null) {
            float cx = width / 2f;
            float cy = height / 2f;

            float checkRadius = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    10f,
                    getResources().getDisplayMetrics()
            );

            float iconSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16f,
                    getResources().getDisplayMetrics()
            );

            int bgColor = generateTone(seedColor, 90);
            int iconColor = generateTone(seedColor, 10);

            paint.setColor(bgColor);
            canvas.save();
            canvas.scale(checkScale, checkScale, cx, cy);
            canvas.drawCircle(cx, cy, checkRadius, paint);

            checkDrawable.setTint(iconColor);
            int left = (int) (cx - iconSize / 2f);
            int top = (int) (cy - iconSize / 2f);
            checkDrawable.setBounds(left, top, left + (int) iconSize, top + (int) iconSize);
            checkDrawable.draw(canvas);

            canvas.restore();
        }
    }

    @SuppressLint("RestrictedApi")
    private int generateTone(int baseColor, int tone) {
        @SuppressLint("RestrictedApi") Hct hct = Hct.fromInt(baseColor);
        @SuppressLint("RestrictedApi") Hct toned = Hct.from(hct.getHue(), hct.getChroma(), tone);
        return toned.toInt();
    }
}