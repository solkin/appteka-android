package com.tomclaw.appsend;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ivsolkin on 06.09.16.
 */
public class DonateItem extends AbstractAppItem {

    private View itemView;

    public DonateItem(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener) {
        if(listener != null) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(info);
                }
            });
        }
    }
}
