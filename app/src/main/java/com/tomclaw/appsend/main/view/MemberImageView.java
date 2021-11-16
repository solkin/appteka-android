package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.dto.UserIcon;

/**
 * Created by Solkin on 20.08.2015.
 */
public class MemberImageView extends SVGImageView {

    private PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;

    public MemberImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setUserIcon(UserIcon userIcon) {
        int color = Color.parseColor(userIcon.getColor());
        String avatar = userIcon.getIcon();

        setColorFilter(0xffffffff, mode);
        try {
            setSVG(SVG.getFromString(avatar));
        } catch (SVGParseException e) {
            e.printStackTrace();
        }

        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.avatar_background);
        if (drawable != null) {
            drawable.setColorFilter(color, mode);
            setBackgroundDrawable(drawable);
        }
    }
}
