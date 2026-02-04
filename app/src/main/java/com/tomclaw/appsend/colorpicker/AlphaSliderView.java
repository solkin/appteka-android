package com.tomclaw.appsend.colorpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class AlphaSliderView extends View {

    private Paint paint;
    private Paint checkPaint;
    private Paint selectorPaint;
    private int color = Color.RED; // Base color
    private float alpha = 1f;
    private final OnAlphaChangeListener listener;

    public interface OnAlphaChangeListener {
        void onAlphaChanged(float alpha);
    }

    public AlphaSliderView(Context context, AttributeSet attrs, int defStyleAttr, OnAlphaChangeListener listener) {
        super(context, attrs, defStyleAttr);
        this.listener = listener;
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        selectorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setStrokeWidth(6f);
        selectorPaint.setColor(Color.WHITE);
        selectorPaint.setShadowLayer(4f, 0, 2f, Color.argb(80, 0, 0, 0));
        setLayerType(LAYER_TYPE_SOFTWARE, selectorPaint);

        createCheckerboard();
    }

    private void createCheckerboard() {
        int size = 20;
        Bitmap bitmap = Bitmap.createBitmap(size * 2, size * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        canvas.drawRect(0, 0, size * 2, size * 2, p);
        p.setColor(Color.LTGRAY);
        canvas.drawRect(0, 0, size, size, p);
        canvas.drawRect(size, size, size * 2, size * 2, p);

        checkPaint.setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
    }

    public void setColor(int color) {
        this.color = color;
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // 1. Draw Checkerboard background
        canvas.drawRoundRect(0, 0, width, height, height / 2f, height / 2f, checkPaint);

        // 2. Draw Gradient (Transparent -> Full Color)
        // Remove alpha from base color for the gradient
        int noAlphaColor = (color & 0x00FFFFFF) | 0xFF000000;

        @SuppressLint("DrawAllocation") Shader gradient = new LinearGradient(0, 0, width, 0,
                Color.TRANSPARENT, noAlphaColor,
                Shader.TileMode.CLAMP);
        paint.setShader(gradient);

        canvas.drawRoundRect(0, 0, width, height, height / 2f, height / 2f, paint);

        // Selector
        float x = alpha * width;
        x = Math.max(height / 2f, Math.min(x, width - height / 2f));

        @SuppressLint("DrawAllocation") Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(Color.WHITE);
        fillPaint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x, height / 2f, height * 0.6f, selectorPaint);
        canvas.drawCircle(x, height / 2f, height * 0.6f, fillPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = Math.max(0, Math.min(event.getX(), getWidth()));
                alpha = x / getWidth();

                if (listener != null) {
                    listener.onAlphaChanged(alpha);
                }
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }
}