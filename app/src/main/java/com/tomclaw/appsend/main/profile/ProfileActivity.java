package com.tomclaw.appsend.main.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
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
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
    TextView memberLastSeen;

    @ViewById
    Button changeRoleButton;

    @ViewById
    LinearLayout detailsContainer;

    @Extra
    long userId;

    @InstanceState
    Profile profile;

    @InstanceState
    int[] grantRoles;

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(getString(R.string.change_role));
        for (int role : grantRoles) {
            if (role != profile.getRole()) {
                int roleName = RoleHelper.getRoleName(role);
                menu.add(Menu.NONE, role, Menu.NONE, roleName);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        showProgress();
        changeRole(item.getItemId());
        return super.onContextItemSelected(item);
    }

    @Click(R.id.change_role_button)
    void onChangeRoleClicked() {
        registerForContextMenu(changeRoleButton);
        openContextMenu(changeRoleButton);
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

    private void changeRole(int role) {
        String guid = session.getUserData().getGuid();
        String stringUserId = userId == 0 ? null : String.valueOf(userId);
        Call<EmpowerResponse> call = serviceHolder.getService().empower(1, guid, role, stringUserId);
        call.enqueue(new Callback<EmpowerResponse>() {
            @Override
            public void onResponse(Call<EmpowerResponse> call, final Response<EmpowerResponse> response) {
                final EmpowerResponse empowerResponse = response.body();
                if (response.isSuccessful() && empowerResponse != null) {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            showProgress();
                            loadProfile();
                        }
                    });
                } else {
                    MainExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            onEmpowerError();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<EmpowerResponse> call, Throwable t) {
                MainExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        onEmpowerError();
                    }
                });
            }
        });
    }

    private void onEmpowerError() {
        showContent();
        Snackbar.make(viewFlipper, R.string.unable_to_change_role, Snackbar.LENGTH_LONG).show();
    }

    private void onLoaded(ProfileResponse body) {
        isError = false;
        profile = body.getProfile();
        grantRoles = body.getGrantRoles();
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
        long lastSeen = TimeUnit.SECONDS.toMillis(profile.getLastSeen());
        String lastSeenString = formatLastSeen(lastSeen);
        if (lastSeenString != null) {
            memberLastSeen.setText(lastSeenString);
            memberLastSeen.setVisibility(View.VISIBLE);
        } else {
            memberLastSeen.setVisibility(View.GONE);
        }
        detailsContainer.removeAllViews();
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.apps_uploaded), String.valueOf(profile.getFilesCount())));
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.messages_wrote), String.valueOf(profile.getMsgCount())));
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.apps_rated), String.valueOf(profile.getRatingsCount())));
        detailsContainer.addView(DetailsItem_.build(this)
                .setDetails(getString(R.string.moderators_assigned), String.valueOf(profile.getModeratorsCount())));
        boolean canChangeRole = false;
        if (session.getUserData().getUserId() != profile.getUserId() && grantRoles.length > 0) {
            for (int role : grantRoles) {
                if (role != profile.getRole()) {
                    canChangeRole = true;
                    break;
                }
            }
        }
        changeRoleButton.setVisibility(canChangeRole ? View.VISIBLE : View.GONE);
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

    private String formatLastSeen(long lastSeen) {
        Calendar lastSeenTime = Calendar.getInstance();
        lastSeenTime.setTimeInMillis(lastSeen);
        Calendar now = Calendar.getInstance();
        boolean isOnline = MILLISECONDS.toMinutes(System.currentTimeMillis() - lastSeen) < 15;
        boolean isToday = DateUtils.isToday(lastSeen);
        boolean isYesterday = now.get(Calendar.DATE) - lastSeenTime.get(Calendar.DATE) == 1;
        String lastSeenString = null;
        if (isOnline) {
            lastSeenString = getString(R.string.online);
        } else if (isToday) {
            lastSeenString = getString(R.string.today, timeHelper().getFormattedTime(lastSeen));
        } else if (isYesterday) {
            lastSeenString = getString(R.string.yesterday);
        } else if (lastSeen != 0) {
            lastSeenString = getString(R.string.last_seen, timeHelper().getFormattedDate(lastSeen));
        }
        return lastSeenString;
    }

}
