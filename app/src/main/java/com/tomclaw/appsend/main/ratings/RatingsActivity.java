package com.tomclaw.appsend.main.ratings;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.screen.profile.ProfileActivityKt.createProfileActivityIntent;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.adapter.MenuAdapter;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.dto.RatingItem;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.user.api.UserBrief;
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

    @Bean
    Session session;

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

    private void reloadRatings() {
        ratingItems = null;
        showProgress();
        loadRatings();
    }

    private void loadRatings() {
        isLoading = true;
        isError = false;
        int rateId = 0;
        if (ratingItems != null && ratingItems.size() > 0) {
            RatingItem lastRatingItem = ratingItems.get(ratingItems.size() - 1);
            rateId = lastRatingItem.getRateId();
        }
        Call<ApiResponse<RatingsResponse>> call = serviceHolder.getService().getRatings(appId, rateId, 7);
        call.enqueue(new Callback<ApiResponse<RatingsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<RatingsResponse>> call, final Response<ApiResponse<RatingsResponse>> response) {
                MainExecutor.execute(() -> {
                    if (response.isSuccessful()) {
                        onLoaded(response.body().getResult());
                    } else {
                        onLoadingError();
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<RatingsResponse>> call, Throwable t) {
                MainExecutor.execute(() -> onLoadingError());
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
        Call<ApiResponse<UserBrief>> call = serviceHolder.getService().getUserBrief(null);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<UserBrief>> call, final Response<ApiResponse<UserBrief>> response) {
                MainExecutor.execute(() -> {
                    if (response.isSuccessful()) {
                        onReviewClicks(item, response.body().getResult());
                    } else {
                        onReviewClicks(item, null);
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<UserBrief>> call, Throwable t) {
                onReviewClicks(item, null);
            }
        });


    }

    private void onReviewClicks(RatingItem item, UserBrief brief) {
        if (brief != null && (item.getUserId() == brief.getUserId() || brief.getRole() >= 200)) {
            ListAdapter menuAdapter = new MenuAdapter(this, R.array.rating_actions_titles, R.array.rating_actions_icons);
            new AlertDialog.Builder(this)
                    .setAdapter(menuAdapter, (dialog, which) -> {
                        switch (which) {
                            case 0: {
                                Intent intent = createProfileActivityIntent(this, (int) item.getUserId());
                                startActivity(intent);
                                trackEvent("rating-profile");
                                break;
                            }
                            case 1: {
                                Call<ApiResponse<VoidResponse>> call = serviceHolder.getService().deleteRating(item.getRateId());
                                call.enqueue(new Callback<ApiResponse<VoidResponse>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<VoidResponse>> call, final Response<ApiResponse<VoidResponse>> response) {
                                        MainExecutor.execute(() -> {
                                            if (response.isSuccessful()) {
                                                Snackbar.make(ratingsView, R.string.rating_deleted, Snackbar.LENGTH_LONG).show();
                                                reloadRatings();
                                            } else {
                                                Snackbar.make(ratingsView, R.string.error_rating_deletion, Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse<VoidResponse>> call, Throwable t) {
                                        MainExecutor.execute(() -> onLoadingError());
                                    }
                                });
                                trackEvent("rating-delete");
                                break;
                            }
                        }
                    }).show();
        } else {
            Intent intent = createProfileActivityIntent(this, (int) item.getUserId());
            startActivity(intent);
        }
    }

}
