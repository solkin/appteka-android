package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.tomclaw.appsend.AppInfo;
import com.tomclaw.appsend.R;

/**
 * Created by Solkin on 10.12.2014.
 */
public class ApkItem extends AppItem {

    private TextView appLocation;

    public ApkItem(View itemView) {
        super(itemView);
        appLocation = (TextView) itemView.findViewById(R.id.apk_location);
    }

    public void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener) {
        super.bind(context, info, listener);
        appLocation.setText(info.getPath());
    }
}
