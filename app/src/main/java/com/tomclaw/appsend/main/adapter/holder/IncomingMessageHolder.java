package com.tomclaw.appsend.main.adapter.holder;

import static com.tomclaw.appsend.util.StringUtil.formatQuote;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.util.BubbleColorDrawable;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.Corner;
import com.tomclaw.appsend.view.UserIconView;
import com.tomclaw.appsend.view.UserIconViewImpl;

/**
 * Created by solkin on 17/04/16.
 */
public class IncomingMessageHolder extends AbstractMessageHolder {

    private final View rootView;
    private final UserIconView memberAvatar;
    private final TextView text;
    private final View bubbleBack;
    private final TextView time;
    private final TextView date;

    private final BubbleColorDrawable textBackground;

    public IncomingMessageHolder(View itemView) {
        super(itemView);

        rootView = itemView;
        memberAvatar = new UserIconViewImpl(itemView.findViewById(R.id.member_icon));
        text = itemView.findViewById(R.id.inc_text);
        bubbleBack = itemView.findViewById(R.id.inc_bubble_back);
        time = itemView.findViewById(R.id.inc_time);
        date = itemView.findViewById(R.id.message_date);

        int bubbleColor = ColorHelper.getAttributedColor(itemView.getContext(), R.attr.discuss_bubble_color);
        textBackground = new BubbleColorDrawable(itemView.getContext(), bubbleColor, Corner.LEFT);
    }

    // Beeeee comment.
    // nwhthy5 jmkoohre fgbyufgu jig

    @Override
    public void bind(final Message message, Message prevMessage,
                     final ChatAdapter.MessageClickListener clickListener) {
        int memberColor = Color.parseColor(message.getUserIcon().getColor());
        memberAvatar.setUserIcon(message.getUserIcon());
        boolean hasMessage = !TextUtils.isEmpty(message.getText());
        String string = message.getText();
        SpannableStringBuilder spannable = formatQuote(string);
        text.setText(spannable);
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

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onMessageClicked(message);
            }
        });
    }


}
