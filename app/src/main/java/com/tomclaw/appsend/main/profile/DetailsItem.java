package com.tomclaw.appsend.main.profile;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tomclaw.appsend.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import static com.tomclaw.appsend.util.ColorHelper.getAttributedColor;

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

    public DetailsItem setClickListener(OnClickListener clickListener) {
        SpannableString content = new SpannableString(detailText.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        detailText.setText(content);
        detailText.setTextColor(getAttributedColor(getContext(), android.R.attr.textColorLink));
        setOnClickListener(clickListener);
        return this;
    }

}
