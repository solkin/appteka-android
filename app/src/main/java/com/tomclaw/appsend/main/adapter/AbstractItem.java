package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tomclaw.appsend.BaseItem;
import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 06.09.16.
 */

public abstract class AbstractItem extends RecyclerView.ViewHolder {
    public AbstractItem(View itemView) {
        super(itemView);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int margin = itemView.getResources().getDimensionPixelSize(R.dimen.app_item_margin);
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            p.setMargins(margin, margin / 3, margin, 0); // get rid of margins since shadow area is now the margin
            itemView.setLayoutParams(p);
        }
    }

    public abstract void bind(Context context, final BaseItem item, final BaseItemAdapter.BaseItemClickListener listener);
}
