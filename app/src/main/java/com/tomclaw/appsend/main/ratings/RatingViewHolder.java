package com.tomclaw.appsend.main.ratings;

import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.dto.RatingItem;
import com.tomclaw.appsend.main.view.MemberImageView;

import static com.tomclaw.appsend.main.ratings.RatingsHelper.tintRatingIndicator;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by solkin on 03.08.17.
 */
class RatingViewHolder extends RecyclerView.ViewHolder {

    private View itemView;
    private MemberImageView memberImageView;
    private AppCompatRatingBar ratingView;
    private TextView dateView;
    private TextView commentView;

    RatingViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        memberImageView = (MemberImageView) itemView.findViewById(R.id.member_avatar);
        ratingView = (AppCompatRatingBar) itemView.findViewById(R.id.rating_view);
        dateView = (TextView) itemView.findViewById(R.id.date_view);
        commentView = (TextView) itemView.findViewById(R.id.comment_view);
    }

    void bind(final RatingItem item, boolean isLast, OnNextPageListener listener) {
        tintRatingIndicator(itemView.getContext(), ratingView);
        memberImageView.setMemberId(item.getUserId());
        ratingView.setRating(item.getScore());
        dateView.setText(timeHelper().getFormattedDate(SECONDS.toMillis(item.getTime())));
        commentView.setText(item.getText());
    }
}
