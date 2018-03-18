package com.tomclaw.appsend.main.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.RoleHelper;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;

/**
 * Created by solkin on 16/03/2018.
 */
@EActivity(R.layout.profile_activity)
public class ProfileActivity extends AppCompatActivity {

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @ViewById
    Toolbar toolbar;

    @ViewById
    ViewFlipper viewFlipper;

    @ViewById
    SwipeRefreshLayout swipeRefresh;

    @ViewById
    TextView errorText;

    @ViewById
    Button retryButton;

    @ViewById
    MemberImageView memberAvatar;

    @ViewById
    TextView memberName;

    @ViewById
    TextView memberRole;

    @ViewById
    TextView memberJoined;

    @ViewById
    LinearLayout detailsContainer;

    @Extra
    long userId;

    @InstanceState
    Profile profile;

    @InstanceState
    boolean isError;

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

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadProfile();
            }
        });

        if (profile != null) {
            bindProfile();
        } else if (isError) {
            showError();
        } else {
            showProgress();
            loadProfile();
        }
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    private void loadProfile() {
        String guid = session.getUserData().getGuid();
        String stringUserId = userId == 0 ? null : String.valueOf(userId);
        Call<ProfileResponse> call = serviceHolder.getService().getProfile(1, guid, stringUserId);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, final Response<ProfileResponse> response) {
                final ProfileResponse profileResponse = response.body();
                if (response.isSuccessful() && profileResponse != null) {
                    session.getUserData().onRoleUpdated(profileResponse.getProfile().getRole());
                    session.getUserHolder().store();
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            onLoaded(profileResponse);
                        }
                    });
                } else {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            onLoadingError();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onLoadingError();
                    }
                });
            }
        });
    }

    private void onLoaded(ProfileResponse body) {
        isError = false;
        profile = body.getProfile();
        bindProfile();
    }

    private void onLoadingError() {
        isError = true;
        showError();
    }

    private void bindProfile() {
        memberAvatar.setMemberId(profile.getUserId());
        memberName.setText(memberImageHelper().getName(profile.getUserId(), isThreadOwner()));
        memberRole.setText(RoleHelper.getRoleName(profile.getRole()));
        memberJoined.setText(getString(R.string.joined_date,
                timeHelper().getFormattedDate(TimeUnit.SECONDS.toMillis(profile.getJoinTime()))));
        detailsContainer.removeAllViews();
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.apps_uploaded), String.valueOf(profile.getFilesCount())));
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.messages_wrote), String.valueOf(profile.getMsgCount())));
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.apps_rated), String.valueOf(profile.getRatingsCount())));
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.moderators_assigned), String.valueOf(profile.getModeratorsCount())));
        showContent();
        swipeRefresh.setRefreshing(false);
    }

    public boolean isThreadOwner() {
        return profile.getUserId() == 1;
    }

    private void showError() {
        errorText.setText(R.string.profile_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress();
                loadProfile();
            }
        });
        viewFlipper.setDisplayedChild(2);
        swipeRefresh.setRefreshing(false);
    }

    private void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    private void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

}
