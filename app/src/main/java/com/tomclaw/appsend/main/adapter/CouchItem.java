package com.tomclaw.appsend.main.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tomclaw.appsend.AppInfo;
import com.tomclaw.appsend.R;

/**
 * Created by ivsolkin on 06.09.16.
 */
public class CouchItem extends AbstractAppItem {

    private View itemView;
    private TextView couchText;
    private Button couchButton;

    public CouchItem(View itemView) {
        super(itemView);
        this.itemView = itemView;
        couchText = (TextView) itemView.findViewById(R.id.couch_text);
        couchButton = (Button) itemView.findViewById(R.id.couch_button);
    }

    public void bind(Context context, final AppInfo info, final AppInfoAdapter.AppItemClickListener listener) {
        if(listener != null) {
            couchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClicked(info);
                }
            });
        }
        couchText.setText(R.string.install_screen_apk_found);
        couchButton.setText(R.string.got_it);
    }
}
