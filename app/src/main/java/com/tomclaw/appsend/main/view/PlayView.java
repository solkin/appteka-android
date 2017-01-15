package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 15.01.17.
 */
public class PlayView extends FrameLayout {

    private TextView countView;
    private TextView descriptionView;
    private TextView commentView;

    public PlayView(Context context) {
        super(context);
    }

    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.play_view, this);
        countView = (TextView) findViewById(R.id.play_count);
        descriptionView = (TextView) findViewById(R.id.play_description);
        commentView = (TextView) findViewById(R.id.play_comment);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PlayView);
        String count = array.getString(R.styleable.PlayView_play_count);
        String description = array.getString(R.styleable.PlayView_play_description);
        String comment = array.getString(R.styleable.PlayView_play_comment);
        array.recycle();
        setCount(count);
        setDescription(description);
        setComment(comment);
    }

    public void setCount(String count) {
        bindText(countView, count);
    }

    public void setDescription(String description) {
        bindText(descriptionView, description);
    }

    public void setComment(String comment) {
        bindText(commentView, comment);
    }

    private void bindText(TextView textView, String text) {
        if (textView != null) {
            if (TextUtils.isEmpty(text)) {
                textView.setVisibility(GONE);
            } else {
                textView.setText(text);
                textView.setVisibility(VISIBLE);
            }
        }
    }
}
