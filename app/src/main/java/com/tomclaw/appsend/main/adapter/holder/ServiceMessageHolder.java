package com.tomclaw.appsend.main.adapter.holder;

import static com.tomclaw.appsend.util.TimeHelper.timeHelper;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.GlobalProvider;
import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.view.UserIconView;
import com.tomclaw.appsend.view.UserIconViewImpl;

/**
 * Created by solkin on 17/04/16.
 */
public class ServiceMessageHolder extends AbstractMessageHolder {

    private View rootView;
    private View dateContainer;
    private TextView date;
    private UserIconView memberAvatar;
    private TextView serviceText;

    public ServiceMessageHolder(View itemView) {
        super(itemView);

        rootView = itemView;
        dateContainer = itemView.findViewById(R.id.date_container);
        date = itemView.findViewById(R.id.message_date);
        memberAvatar = new UserIconViewImpl(itemView.findViewById(R.id.member_icon));
        serviceText = itemView.findViewById(R.id.service_text);
    }

    @Override
    public void bind(final Message message, Message prevMessage,
                     final ChatAdapter.MessageClickListener clickListener) {
        String messageDateText = timeHelper().getFormattedDate(message.getTime());
        boolean dateVisible = true;
        if (prevMessage != null) {
            dateVisible = prevMessage.getTime() != Long.MAX_VALUE && message.getTime() != Long.MAX_VALUE &&
                    !messageDateText.equals(timeHelper().getFormattedDate(prevMessage.getTime()));
        }
        date.setText(timeHelper().getFormattedDate(message.getTime()));
        dateContainer.setVisibility(dateVisible ? View.VISIBLE : View.GONE);

        int memberColor = Color.parseColor(message.getUserIcon().getColor());
        memberAvatar.bind(message.getUserIcon());
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

        rootView.setOnClickListener(v -> clickListener.onMessageClicked(message));
    }
}
