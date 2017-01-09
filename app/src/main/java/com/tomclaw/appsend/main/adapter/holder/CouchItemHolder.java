package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.BaseItemAdapter;
import com.tomclaw.appsend.main.item.CouchItem;

/**
 * Created by ivsolkin on 06.09.16.
 */
public class CouchItemHolder extends AbstractItemHolder<CouchItem> {

    private View itemView;
    private TextView couchText;
    private Button couchButton;

    public CouchItemHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        couchText = (TextView) itemView.findViewById(R.id.couch_text);
        couchButton = (Button) itemView.findViewById(R.id.couch_button);
    }

    public void bind(Context context, final CouchItem item, final BaseItemAdapter.BaseItemClickListener<CouchItem> listener) {
        if(listener != null) {
            couchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(item);
                }
            });
        }
        couchText.setText(item.getCouchText());
        couchButton.setText(item.getButtonText());
    }
}
