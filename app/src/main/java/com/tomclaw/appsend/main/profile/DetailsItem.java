package com.tomclaw.appsend.main.profile;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.tomclaw.appsend.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by solkin on 18/03/2018.
 */
@EViewGroup(R.layout.detail_item)
public class DetailsItem extends RelativeLayout {

    @ViewById
    ImageView detailIcon;

    @ViewById
    TextView detailText;

    @ViewById
    TextView detailValue;

    @ViewById
    TextView detailCounter;

    @ViewById
    View detailDivider;

    public DetailsItem(Context context) {
        super(context);
    }

    public DetailsItem setDetails(
            @DrawableRes int icon,
            @ColorRes int color,
            String text,
            String value,
            boolean isLast
    ) {
        detailIcon.setImageResource(icon);
        detailIcon.setColorFilter(getResources().getColor(color));
        detailText.setText(text);
        detailValue.setText(value);
        detailDivider.setVisibility(isLast ? INVISIBLE : VISIBLE);
        return this;
    }

    public void setCounter(String counter) {
        if (TextUtils.isEmpty(counter)) {
            detailCounter.setVisibility(GONE);
        } else {
            detailCounter.setText(counter);
            detailCounter.setVisibility(VISIBLE);
        }
    }

    public DetailsItem setClickListener(OnClickListener clickListener) {
        setOnClickListener(clickListener);
        return this;
    }

}
