package com.tomclaw.appsend.main.ratings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.RatingItem;
import com.tomclaw.appsend.main.profile.ProfileActivity_;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Igor on 22.10.2017.
 */
@EActivity(R.layout.ratings_activity)
public class RatingsActivity extends AppCompatActivity implements RatingsListener {

    @Bean
    StoreServiceHolder serviceHolder;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    RecyclerView ratingsView;

    @ViewById
    TextView errorText;

    @ViewById
    Button retryButton;

    @InstanceState
    ArrayList<RatingItem> ratingItems;

    @InstanceState
    boolean isError;

    @InstanceState
    boolean isLoading;

    @InstanceState
    boolean isLoadedAll;

    @Extra
    String appId;

    private RatingsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    void init() {
        ThemeHelper.updateStatusBar(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        adapter = new RatingsAdapter(this);
        adapter.setHasStableIds(true);
        adapter.setListener(this);
        ratingsView.setLayoutManager(layoutManager);
        ratingsView.setAdapter(adapter);

        if (ratingItems == null) {
            showProgress();
            loadRatings();
        } else {
            updateRatings();
            showContent();
        }
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    private void loadRatings() {
        isLoading = true;
        isError = false;
        int rateId = 0;
        if (ratingItems != null && ratingItems.size() > 0) {
            RatingItem lastRatingItem = ratingItems.get(ratingItems.size() - 1);
            rateId = lastRatingItem.getRateId();
        }
        Call<RatingsResponse> call = serviceHolder.getService().getRatings(1, appId, rateId, 7);
        call.enqueue(new Callback<RatingsResponse>() {
            @Override
            public void onResponse(Call<RatingsResponse> call, final Response<RatingsResponse> response) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        if (response.isSuccessful()) {
                            onLoaded(response.body());
                        } else {
                            onLoadingError();
                        }
                    }
                });
            }

            @Override
            public void onFailure(Call<RatingsResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onLoadingError();
                    }
                });
            }
        });
    }

    private void onLoaded(RatingsResponse body) {
        isLoading = false;
        isError = false;
        if (body.getRatings().isEmpty()) {
            isLoadedAll = true;
        }
        if (ratingItems == null) {
            ratingItems = new ArrayList<>(body.getRatings());
        } else {
            ratingItems.addAll(body.getRatings());
        }
        updateRatings();
    }

    private void onLoadingError() {
        isLoading = false;
        isError = true;
        if (ratingItems == null) {
            showError();
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void updateRatings() {
        adapter.setItems(ratingItems);
        adapter.notifyDataSetChanged();
        showContent();
    }

    private void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    private void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

    private void showError() {
        errorText.setText(R.string.load_ratings_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                loadRatings();
            }
        });
        viewFlipper.setDisplayedChild(2);
    }

    @Override
    public int onNextPage() {
        if (isError) {
            return RatingsListener.STATE_FAILED;
        } else if (isLoading) {
            return RatingsListener.STATE_LOADING;
        } else if (isLoadedAll) {
            return RatingsListener.STATE_LOADED;
        } else {
            loadRatings();
            return RatingsListener.STATE_LOADING;
        }
    }

    @Override
    public void onRetry() {
        loadRatings();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(RatingItem item) {
        ProfileActivity_.intent(this).userId(item.getUserId()).start();
    }
}
