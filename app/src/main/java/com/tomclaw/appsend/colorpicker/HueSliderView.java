package com.tomclaw.appsend.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class HueSliderView extends View {

    private Paint paint;
    private Paint selectorPaint;

    private float currentHue = 0f;
    private OnHueChangeListener listener;

    private boolean isTouchActive = false;

    public interface OnHueChangeListener {
        void onHueChanged(float hue);
    }

    public HueSliderView(Context context) {
        super(context);
        init();
    }

    public HueSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HueSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(6f);
        selectorPaint.setColor(Color.WHITE);
        selectorPaint.setShadowLayer(4f, 0, 2f, Color.argb(80, 0, 0, 0));

        setLayerType(LAYER_TYPE_SOFTWARE, selectorPaint);
    }

    public void setHue(float hue) {
        currentHue = hue;
        invalidate();
    }

    public void setOnHueChangeListener(OnHueChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        @SuppressLint("DrawAllocation") int[] colors = new int[]{
                0xFFFF0000,
                0xFFFFFF00,
                0xFF00FF00,
                0xFF00FFFF,
                0xFF0000FF,
                0xFFFF00FF,
                0xFFFF0000
        };

        @SuppressLint("DrawAllocation") Shader shader = new LinearGradient(0, 0, width, 0, colors, null, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        float radiusBg = height / 2f;
        canvas.drawRoundRect(0, 0, width, height, radiusBg, radiusBg, paint);

        float selectorX = (currentHue / 360f) * width;
        selectorX = Math.max(radiusBg, Math.min(selectorX, width - radiusBg));

        float radius = height * 0.35f;
        float cy = height / 2f;

        @SuppressLint("DrawAllocation") Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.WHITE);

        canvas.drawCircle(selectorX, cy, radius, fillPaint);
        canvas.drawCircle(selectorX, cy, radius, selectorPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                isTouchActive = true;
                updateHueFromTouch(x);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isTouchActive) updateHueFromTouch(x);
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouchActive = false;
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void updateHueFromTouch(float x) {
        float w = getWidth();

        if (x < 0) x = 0;
        if (x > w) x = w;

        float hue = (x / w) * 360f;

        if (hue != currentHue) {
            currentHue = hue;
            if (listener != null) listener.onHueChanged(currentHue);
            invalidate();
        }
    }
}