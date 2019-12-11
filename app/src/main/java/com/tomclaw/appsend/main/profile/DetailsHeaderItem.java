package com.tomclaw.appsend.main.profile;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tomclaw.appsend.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by solkin on 11/12/2018.
 */
@EViewGroup(R.layout.detail_header_item)
public class DetailsHeaderItem extends RelativeLayout {

    @ViewById
    TextView headerText;

    public DetailsHeaderItem(Context context) {
        super(context);
    }

    public DetailsHeaderItem setDetails(String text) {
        headerText.setText(text);
        return this;
    }

}
