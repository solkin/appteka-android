package com.tomclaw.appsend.main.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import com.tomclaw.appsend.R;

import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;

/**
 * Created by Solkin on 20.08.2015.
 */
public class MemberImageView extends AppCompatImageView {

    private long userId;

    private PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;

    public MemberImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public long getUserId() {
        return userId;
    }

    public boolean isThreadOwner() {
        return userId == 1;
    }

    public void setMemberId(long userId) {
        this.userId = userId;

        int color = memberImageHelper().getColor(userId);
        int avatar = memberImageHelper().getAvatar(userId, isThreadOwner());

        Drawable drawable = AppCompatResources.getDrawable(getContext(), R.drawable.avatar_background);
        if (drawable != null) {
            drawable.setColorFilter(color, mode);
            setBackgroundDrawable(drawable);
        }

        Drawable avatarDrawable = AppCompatResources.getDrawable(getContext(), avatar);
        if (avatarDrawable != null) {
            avatarDrawable.setColorFilter(0xffffffff, mode);
            setImageDrawable(avatarDrawable);
        }
    }
}
