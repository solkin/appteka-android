package com.tomclaw.appsend.main.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        if (profile != null) {
            bindProfile();
        } else if (isError) {
            showError();
        } else {
            loadProfile();
        }
    }

    @OptionsItem(android.R.id.home)
    boolean actionHome() {
        onBackPressed();
        return true;
    }

    private void loadProfile() {
        showProgress();
        String guid = session.getUserData().getGuid();
        Call<ProfileResponse> call = serviceHolder.getService().getProfile(1, guid);
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, final Response<ProfileResponse> response) {
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
        showContent();
    }

    private void showError() {
        errorText.setText(R.string.load_ratings_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProfile();
            }
        });
        viewFlipper.setDisplayedChild(2);
    }

    private void showProgress() {
        viewFlipper.setDisplayedChild(0);
    }

    private void showContent() {
        viewFlipper.setDisplayedChild(1);
    }

}
