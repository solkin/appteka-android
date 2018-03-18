package com.tomclaw.appsend.main.profile;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tomclaw.appsend.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by solkin on 18/03/2018.
 */
@EViewGroup(R.layout.detail_item)
public class DetailsItem extends RelativeLayout {

    @ViewById
    TextView detailText;

    @ViewById
    TextView detailValue;

    public DetailsItem(Context context) {
        super(context);
    }

    public DetailsItem setDetails(String text, String value) {
        detailText.setText(text);
        detailValue.setText(value);
        return this;
    }

}
