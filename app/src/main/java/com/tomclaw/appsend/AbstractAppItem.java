package com.tomclaw.appsend;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ivsolkin on 06.09.16.
 */

public abstract class AbstractAppItem extends RecyclerView.ViewHolder {
    public AbstractAppItem(View itemView) {
        super(itemView);
    }

    public abstract void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener);
}
