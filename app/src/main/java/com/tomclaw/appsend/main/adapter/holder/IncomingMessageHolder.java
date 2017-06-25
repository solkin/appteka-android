package com.tomclaw.appsend.main.adapter.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.util.BubbleColorDrawable;
import com.tomclaw.appsend.util.Corner;

import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;

/**
 * Created by solkin on 17/04/16.
 */
public class IncomingMessageHolder extends AbstractMessageHolder {

    private MemberImageView memberAvatar;
    private TextView text;
    private View bubbleBack;
    private TextView time;
    private TextView date;

    private BubbleColorDrawable textBackground;

    public IncomingMessageHolder(View itemView) {
        super(itemView);

        memberAvatar = (MemberImageView) itemView.findViewById(R.id.member_avatar);
        text = (TextView) itemView.findViewById(R.id.inc_text);
        bubbleBack = itemView.findViewById(R.id.inc_bubble_back);
        time = (TextView) itemView.findViewById(R.id.inc_time);
        date = (TextView) itemView.findViewById(R.id.message_date);

        int bubbleColor = itemView.getResources().getColor(android.R.color.white);
        textBackground = new BubbleColorDrawable(itemView.getContext(), bubbleColor, Corner.LEFT);
    }

    // Beeeee comment.
    // nwhthy5 jmkoohre fgbyufgu jig

    @Override
    public void bind(final Message message, Message prevMessage) {
        int memberColor = memberImageHelper().getColor(message.getUserId());
        memberAvatar.setMemberId(message.getUserId());
        boolean hasMessage = !TextUtils.isEmpty(message.getText());
        text.setText(message.getText());
        text.setTextColor(memberColor);
        bubbleBack.setVisibility(hasMessage ? View.VISIBLE : View.GONE);
        bubbleBack.setBackgroundDrawable(textBackground);
        time.setText(timeHelper().getFormattedTime(message.getTime()));
        time.setVisibility(message.getTime() != Long.MAX_VALUE ? View.VISIBLE : View.INVISIBLE);
        String messageDateText = timeHelper().getFormattedDate(message.getTime());
        boolean dateVisible = true;
        if (prevMessage != null) {
            dateVisible = prevMessage.getTime() != Long.MAX_VALUE && message.getTime() != Long.MAX_VALUE &&
                    !messageDateText.equals(timeHelper().getFormattedDate(prevMessage.getTime()));
        }
        date.setText(timeHelper().getFormattedDate(message.getTime()));
        date.setVisibility(dateVisible ? View.VISIBLE : View.GONE);
    }


}
