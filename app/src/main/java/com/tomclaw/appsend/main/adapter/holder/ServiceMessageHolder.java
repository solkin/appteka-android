package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.main.view.MemberImageView;

import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;

/**
 * Created by solkin on 17/04/16.
 */
public class ServiceMessageHolder extends AbstractMessageHolder {

    private MemberImageView memberAvatar;
    private TextView serviceText;

    public ServiceMessageHolder(View itemView) {
        super(itemView);

        memberAvatar = (MemberImageView) itemView.findViewById(R.id.member_avatar);
        serviceText = (TextView) itemView.findViewById(R.id.service_text);
    }

    @Override
    public void bind(Message message, Message prevMessage) {
        int memberColor = memberImageHelper().getColor(message.getUserId());
        memberAvatar.setMemberId(message.getUserId());
        int messageResId;
        switch (message.getType()) {
            case GlobalProvider.MESSAGE_TYPE_JOINED: {
                messageResId = R.string.member_joined;
                break;
            }
            default: {
                messageResId = 0;
                break;
            }
        }
        if (messageResId != 0) {
            serviceText.setText(messageResId);
            serviceText.setTextColor(memberColor);
            serviceText.setVisibility(View.VISIBLE);
        } else {
            serviceText.setVisibility(View.GONE);
        }
    }
}
