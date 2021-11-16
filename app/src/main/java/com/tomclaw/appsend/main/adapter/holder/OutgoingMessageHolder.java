package com.tomclaw.appsend.main.adapter.holder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.adapter.ChatAdapter;
import com.tomclaw.appsend.main.dto.Message;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.util.BubbleColorDrawable;
import com.tomclaw.appsend.util.ColorHelper;
import com.tomclaw.appsend.util.Corner;

import static com.tomclaw.appsend.util.StringUtil.formatQuote;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;

/**
 * Created by solkin on 17/04/16.
 */
public class OutgoingMessageHolder extends AbstractMessageHolder {

    private View rootView;
    private MemberImageView memberAvatar;
    private TextView text;
    private View bubbleBack;
    private TextView time;
    private TextView date;
    private ImageView delivery;

    private BubbleColorDrawable textBackground;
    private Context context;

    public OutgoingMessageHolder(View itemView) {
        super(itemView);
        this.context = itemView.getContext();

        rootView = itemView;
        memberAvatar = itemView.findViewById(R.id.member_avatar);
        text = itemView.findViewById(R.id.out_text);
        bubbleBack = itemView.findViewById(R.id.out_bubble_back);
        time = itemView.findViewById(R.id.out_time);
        date = itemView.findViewById(R.id.message_date);
        delivery = itemView.findViewById(R.id.message_delivery);

        int bubbleColor = ColorHelper.getAttributedColor(itemView.getContext(), R.attr.discuss_bubble_color);
        textBackground = new BubbleColorDrawable(itemView.getContext(), bubbleColor, Corner.RIGHT);
    }

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

        Drawable drawable;
        Resources resources = context.getResources();
        if (message.getMsgId() == 0) {
            if (message.getPushTime() > 0) {
                drawable = resources.getDrawable(R.drawable.check_circle);
            } else {
                drawable = resources.getDrawable(R.drawable.clock);
            }
        } else {
            drawable = resources.getDrawable(R.drawable.check_all);
        }

        delivery.setImageDrawable(drawable);
        delivery.setColorFilter(memberColor, PorterDuff.Mode.SRC_ATOP);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onMessageClicked(message);
            }
        });
    }
}
