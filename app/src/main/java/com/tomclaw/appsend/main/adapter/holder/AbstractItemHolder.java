package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.item.BaseItem;

/**
 * Created by ivsolkin on 06.09.16.
 */

public abstract class AbstractItemHolder<I extends BaseItem> extends RecyclerView.ViewHolder {
    public AbstractItemHolder(View itemView) {
        super(itemView);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int margin = itemView.getResources().getDimensionPixelSize(R.dimen.app_item_margin);
            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            p.setMargins(margin, margin / 3, margin, 0); // get rid of margins since shadow area is now the margin
            itemView.setLayoutParams(p);
        }
    }

    public abstract void bind(Context context, final I item, final boolean isLast, final BaseItemAdapter.BaseItemClickListener<I> listener);
}
