package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

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
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) getCardView(itemView).getLayoutParams();
            p.setMargins(margin, margin / 3, margin, 0); // get rid of margins since shadow area is now the margin
            getCardView(itemView).setLayoutParams(p);
        }
    }

    View getCardView(View itemView) {
        return itemView;
    }

    public abstract void bind(Context context, final I item, final boolean isLast, final BaseItemAdapter.BaseItemClickListener<I> listener);
}
