package com.tomclaw.appsend;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by ivsolkin on 06.09.16.
 */

public abstract class AbstractAppItem extends RecyclerView.ViewHolder {
    public AbstractAppItem(View itemView) {
        super(itemView);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int margin = itemView.getResources().getDimensionPixelSize(R.dimen.app_item_margin);
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            p.setMargins(margin, margin / 3, margin, 0); // get rid of margins since shadow area is now the margin
            itemView.setLayoutParams(p);
        }
    }

    public abstract void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener);
}
