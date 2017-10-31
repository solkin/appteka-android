package com.tomclaw.appsend.main.ratings;

import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.dto.RatingItem;
import com.tomclaw.appsend.main.view.MemberImageView;

import static com.tomclaw.appsend.main.ratings.RatingsHelper.tintRatingIndicator;
import static com.tomclaw.appsend.main.ratings.RatingsListener.STATE_FAILED;
import static com.tomclaw.appsend.main.ratings.RatingsListener.STATE_LOADED;
import static com.tomclaw.appsend.main.ratings.RatingsListener.STATE_LOADING;
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
    private View progressView;
    private View errorView;
    private View retryButtonView;

    RatingViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        memberImageView = (MemberImageView) itemView.findViewById(R.id.member_avatar);
        ratingView = (AppCompatRatingBar) itemView.findViewById(R.id.rating_view);
        dateView = (TextView) itemView.findViewById(R.id.date_view);
        commentView = (TextView) itemView.findViewById(R.id.comment_view);
        progressView = itemView.findViewById(R.id.item_progress);
        errorView = itemView.findViewById(R.id.error_view);
        retryButtonView = itemView.findViewById(R.id.button_retry);
    }

    void bind(final RatingItem item, boolean isLast, final RatingsListener listener) {
        tintRatingIndicator(itemView.getContext(), ratingView);
        memberImageView.setMemberId(item.getUserId());
        ratingView.setRating(item.getScore());
        dateView.setText(timeHelper().getFormattedDate(SECONDS.toMillis(item.getTime())));
        String text = item.getText();
        commentView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
        commentView.setText(text);
        boolean isProgress = false;
        boolean isError = false;
        if (isLast) {
            int result = listener.onNextPage();
            switch (result) {
                case STATE_LOADING:
                    isProgress = true;
                    isError = false;
                    break;
                case STATE_FAILED:
                    isProgress = false;
                    isError = true;
                    break;
                case STATE_LOADED:
                    isProgress = false;
                    isError = false;
                    break;
            }
            progressView.setVisibility(View.GONE);
            errorView.setVisibility(View.GONE);
        }
        progressView.setVisibility(isProgress ? View.VISIBLE : View.GONE);
        errorView.setVisibility(isError ? View.VISIBLE : View.GONE);
        if (isError) {
            retryButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRetry();
                }
            });
        } else {
            retryButtonView.setOnClickListener(null);
        }
    }
}
