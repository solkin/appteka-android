package com.tomclaw.appsend;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;

/**
 * Created by Solkin on 10.12.2014.
 */
public class AppItem extends LinearLayout {

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");

    private ImageView appIcon;
    private TextView appName;
    private TextView appVersion;
    private TextView appUpdateTime;
    private TextView appSize;

    public AppItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        appIcon = (ImageView) findViewById(R.id.app_icon);
        appName = (TextView) findViewById(R.id.app_name);
        appVersion = (TextView) findViewById(R.id.app_version);
        appUpdateTime = (TextView) findViewById(R.id.app_update_time);
        appSize = (TextView) findViewById(R.id.app_size);
    }

    public void bind(Context context, AppInfo info) {
        appIcon.setImageDrawable(info.getIcon());
        appName.setText(info.getLabel());
        appVersion.setText(info.getVersion());
        if(info.getLastUpdateTime() > 0) {
            appUpdateTime.setVisibility(VISIBLE);
            appUpdateTime.setText(simpleDateFormat.format(info.getLastUpdateTime()));
        } else {
            appUpdateTime.setVisibility(GONE);
        }
        appSize.setText(FileHelper.formatBytes(context.getResources(), info.getSize()));
    }
}
