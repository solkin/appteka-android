package com.tomclaw.appsend.colorpicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SaturationValueView extends View {

    private Paint paint;
    private Paint selectorPaint;
    private Paint borderPaint;

    private float hue = 0f;
    private float saturation = 1f;
    private float value = 1f;

    private final float selectorRadius = 16f;

    private OnColorChangeListener listener;

    private boolean isTouchActive = false;

    public interface OnColorChangeListener {
        void onColorChanged(float saturation, float value);
    }

    public SaturationValueView(Context context) {
        super(context);
        init();
    }

    public SaturationValueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SaturationValueView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(4f);
        selectorPaint.setColor(Color.WHITE);
        selectorPaint.setShadowLayer(4f, 0, 2f, Color.argb(80, 0, 0, 0));

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setColor(Color.LTGRAY);

        setLayerType(LAYER_TYPE_SOFTWARE, selectorPaint);
    }

    public void setHue(float hue) {
        this.hue = hue;
        invalidate();
    }

    public float getSaturation() {
        return saturation;
    }

    public float getValue() {
        return value;
    }

    public void setSaturationValue(float s, float v) {
        saturation = s;
        value = v;
        invalidate();
    }

    public void setOnColorChangeListener(OnColorChangeListener l) {
        this.listener = l;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        if (w <= 0 || h <= 0) return;

        Shader satShader = new LinearGradient(
                0f, 0f, w, 0f,
                Color.WHITE, Color.HSVToColor(new float[]{hue, 1f, 1f}),
                Shader.TileMode.CLAMP
        );

        Shader valShader = new LinearGradient(
                0f, 0f, 0f, h,
                Color.TRANSPARENT, Color.BLACK,
                Shader.TileMode.CLAMP
        );

        paint.setShader(satShader);
        canvas.drawRect(0f, 0f, w, h, paint);

        paint.setShader(valShader);
        canvas.drawRect(0f, 0f, w, h, paint);

        canvas.drawRect(0f, 0f, w, h, borderPaint);

        float x = saturation * w;
        float y = (1f - value) * h;

        canvas.drawCircle(x, y, selectorRadius, selectorPaint);
    }

    private void updateSatValFromTouch(float ex, float ey) {
        float w = getWidth();
        float h = getHeight();

        float x = ex;
        float y = ey;

        if (x < 0f) x = 0f;
        if (x > w) x = w;
        if (y < 0f) y = 0f;
        if (y > h) y = h;

        saturation = x / w;
        value = 1f - (y / h);

        if (listener != null) {
            listener.onColorChanged(saturation, value);
        }

        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                isTouchActive = true;
                updateSatValFromTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isTouchActive) updateSatValFromTouch(event.getX(), event.getY());
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouchActive = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }

        return super.onTouchEvent(event);
    }
}