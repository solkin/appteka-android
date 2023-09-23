package com.tomclaw.appsend.main.profile;

import static com.microsoft.appcenter.analytics.Analytics.trackEvent;
import static com.tomclaw.appsend.util.IntentHelper.shareUrl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.home.HomeActivity;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.ThemeHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by solkin on 16/03/2018.
 */
@SuppressLint("Registered")
@EActivity(R.layout.profile_activity)
public class ProfileActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @FragmentById
    ProfileFragment profileFragment;

    @Extra
    Long userId;

    @InstanceState
    Boolean isShowHomeOnFinish;

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeHelper.updateTheme(this);
        super.onCreate(savedInstanceState);
        boolean isCreateInstance = savedInstanceState == null;
        if (isCreateInstance) {
            Uri data = getIntent().getData();
            if (data != null && data.getHost() != null) {
                if (data.getHost().equals("appteka.store")) {
                    List<String> path = data.getPathSegments();
                    if (path.size() == 2) {
                        userId = Long.parseLong(path.get(1));
                        isShowHomeOnFinish = true;
                    }
                } else if (data.getHost().equals("appsend.store")) {
                    userId = Long.parseLong(data.getQueryParameter("id"));
                    isShowHomeOnFinish = true;
                }
            }
        }
        if (isCreateInstance) {
            trackEvent("open-profile-screen");
        }
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

        profileFragment.setUserId(userId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (profileFragment.profile != null) {
            getMenuInflater().inflate(R.menu.profile_menu, menu);
            if (session.getUserData().getRole() != 300) {
                menu.removeItem(R.id.eliminate);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.share:
                String text = getResources().getString(R.string.user_url, profileFragment.memberName.getText(), profileFragment.profile.getUserId(), profileFragment.profile.getUrl());
                shareUrl(ProfileActivity.this, text);
                trackEvent("share-user-url");
                break;
            case R.id.eliminate:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.eliminate_user_title))
                        .setMessage(getString(R.string.eliminate_user_message))
                        .setNegativeButton(R.string.yes, (dialog, which) -> {
                            profileFragment.showProgress();
                            Call<ApiResponse<EliminateUserResponse>> call = serviceHolder.getService().eliminateUser(userId);
                            call.enqueue(new Callback<ApiResponse<EliminateUserResponse>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<EliminateUserResponse>> call, final Response<ApiResponse<EliminateUserResponse>> response) {
                                    MainExecutor.execute(() -> {
                                        EliminateUserResponse result = response.body().getResult();
                                        if (response.isSuccessful() && result != null) {
                                            String message = getString(R.string.eliminate_user_success, result.getFilesCount(), result.getMessagesCount(), result.getRatingsCount());
                                            Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_LONG).show();
                                            finish();
                                        } else {
                                            Toast.makeText(ProfileActivity.this, R.string.eliminate_user_failed, Toast.LENGTH_LONG).show();
                                            profileFragment.showContent();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<EliminateUserResponse>> call, Throwable t) {
                                    MainExecutor.execute(() -> Toast.makeText(ProfileActivity.this, R.string.eliminate_user_failed, Toast.LENGTH_LONG).show());
                                    profileFragment.showContent();
                                }
                            });
                        })
                        .setPositiveButton(R.string.no, null)
                        .show();
                trackEvent("fire-user");
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isShowHomeOnFinish != null && isShowHomeOnFinish) {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class)
                    .setAction(HomeActivity.ACTION_STORE)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public static Intent createProfileActivityIntent(Context context, int userId) {
        return ProfileActivity_.intent(context).userId((long) userId).get();
    }

}
