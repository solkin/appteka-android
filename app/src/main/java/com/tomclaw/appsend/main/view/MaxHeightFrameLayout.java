package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 17.01.17.
 */
public class MaxHeightFrameLayout extends RelativeLayout {

    private static final int INVALID_HEIGHT = 0;
    private int maxHeight = INVALID_HEIGHT;
    private boolean isOverflow = false;

    public MaxHeightFrameLayout(Context context) {
        super(context);
    }

    public MaxHeightFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaxHeightFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MaxHeightFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightFrameLayout);
            maxHeight = styledAttrs.getDimensionPixelSize(R.styleable.MaxHeightFrameLayout_maxHeight, INVALID_HEIGHT);
            styledAttrs.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        isOverflow = maxHeight != INVALID_HEIGHT && getMeasuredHeight() >= maxHeight;
    }

    public boolean isOverflow() {
        return isOverflow;
    }
}
