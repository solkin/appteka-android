/*
 * Copyright (c) 2016 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.materialratingbar;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Base drawable implementation that supports tinting and alpha handling.
 *
 * <p>This drawable is never inflated from XML and is used only programmatically.
 */
abstract class BaseDrawable extends Drawable implements TintableDrawable {

    protected int mAlpha = 0xFF;
    protected ColorFilter mColorFilter;
    protected ColorStateList mTintList;
    protected PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    protected PorterDuffColorFilter mTintFilter;

    private final DummyConstantState mConstantState = new DummyConstantState();

    @Override
    public int getAlpha() {
        return mAlpha;
    }

    @Override
    public void setAlpha(int alpha) {
        if (mAlpha != alpha) {
            mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public ColorFilter getColorFilter() {
        return mColorFilter;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public void setTint(@ColorInt int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        mTintList = tint;
        if (updateTintFilter()) {
            invalidateSelf();
        }
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        if (updateTintFilter()) {
            invalidateSelf();
        }
    }

    @Override
    public boolean isStateful() {
        return mTintList != null && mTintList.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return updateTintFilter();
    }

    private boolean updateTintFilter() {
        if (mTintList == null || mTintMode == null) {
            boolean hadTintFilter = mTintFilter != null;
            mTintFilter = null;
            return hadTintFilter;
        }

        int tintColor = mTintList.getColorForState(getState(), Color.TRANSPARENT);
        mTintFilter = new PorterDuffColorFilter(tintColor, mTintMode);
        return true;
    }

    /**
     * This method is deprecated by the framework and has no modern replacement. It is kept for
     * compatibility with Drawable contract.
     */
    @SuppressWarnings("deprecation")
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.width() == 0 || bounds.height() == 0) {
            return;
        }

        int saveCount = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        onDraw(canvas, bounds.width(), bounds.height());
        canvas.restoreToCount(saveCount);
    }

    /**
     * Returns the color filter used for drawing, preferring the explicitly set color filter over
     * the tint filter.
     */
    protected ColorFilter getColorFilterForDrawing() {
        return mColorFilter != null ? mColorFilter : mTintFilter;
    }

    protected abstract void onDraw(Canvas canvas, int width, int height);

    /**
     * Workaround for {@link android.graphics.drawable.LayerDrawable} which may call {@code
     * getConstantState().newDrawable()} without null checks.
     *
     * <p>Since this drawable is never inflated from XML, returning itself is safe.
     */
    @Override
    public ConstantState getConstantState() {
        return mConstantState;
    }

    private class DummyConstantState extends ConstantState {

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        @NonNull
        @Override
        public Drawable newDrawable() {
            return BaseDrawable.this;
        }
    }
}
