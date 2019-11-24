package com.tomclaw.appsend.main.profile;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.auth.LoginActivity_;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.home.HomeFragment;
import com.tomclaw.appsend.main.profile.list.FilesActivity_;
import com.tomclaw.appsend.main.view.MemberImageView;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.util.RoleHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static com.tomclaw.appsend.util.MemberImageHelper.memberImageHelper;
import static com.tomclaw.appsend.util.TimeHelper.timeHelper;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@EFragment(R.layout.profile_fragment)
public class ProfileFragment extends HomeFragment {

    private static final int REQUEST_LOGIN = 1;

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
    TextView memberId;

    @ViewById
    TextView memberJoined;

    @ViewById
    TextView memberLastSeen;

    @ViewById
    Button changeRoleButton;

    @ViewById
    Button authButton;

    @ViewById
    LinearLayout detailsContainer;

    @InstanceState
    Profile profile;

    @InstanceState
    int[] grantRoles;

    @InstanceState
    boolean isError;

    @FragmentArg
    long userId;

    @AfterViews
    void init() {
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
            reloadProfile();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
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
        Activity activity = getActivity();
        if (activity != null) {
            activity.openContextMenu(changeRoleButton);
        }
    }

    @Click(R.id.auth_button)
    void onAuthenticateClicked() {
        LoginActivity_.intent(this).startForResult(REQUEST_LOGIN);
    }

    @OnActivityResult(REQUEST_LOGIN)
    void onLoginResult(int resultCode) {
        if (resultCode == RESULT_OK) {
            showProgress();
            loadProfile();
        }
    }

    private void reloadProfile() {
        showProgress();
        loadProfile();
    }

    private void loadProfile() {
        String guid = session.getUserData().getGuid();
        String stringUserId = userId == 0 ? null : String.valueOf(userId);
        Call<ApiResponse<ProfileResponse>> call = serviceHolder.getService().getProfile(1, guid, stringUserId);
        call.enqueue(new Callback<ApiResponse<ProfileResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProfileResponse>> call, final Response<ApiResponse<ProfileResponse>> response) {
                ApiResponse<ProfileResponse> body = response.body();
                if (body != null) {
                    final ProfileResponse profileResponse = body.getResult();
                    if (response.isSuccessful() && profileResponse != null) {
                        session.getUserData().onRoleUpdated(profileResponse.getProfile().getRole());
                        session.getUserHolder().store();
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                onLoaded(profileResponse);
                            }
                        });
                        return;
                    }
                }
                onError();
            }

            @Override
            public void onFailure(Call<ApiResponse<ProfileResponse>> call, Throwable t) {
                onError();
            }

            private void onError() {
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
        Call<ApiResponse<EmpowerResponse>> call = serviceHolder.getService().empower(1, guid, role, stringUserId);
        call.enqueue(new Callback<ApiResponse<EmpowerResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<EmpowerResponse>> call, final Response<ApiResponse<EmpowerResponse>> response) {
                ApiResponse<EmpowerResponse> body = response.body();
                if (body != null) {
                    final EmpowerResponse empowerResponse = body.getResult();
                    if (response.isSuccessful() && empowerResponse != null) {
                        MainExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                reloadProfile();
                            }
                        });
                        return;
                    }
                }
                onError();
            }

            @Override
            public void onFailure(Call<ApiResponse<EmpowerResponse>> call, Throwable t) {
                onError();
            }

            private void onError() {
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
        if (!isVisible()) return;
        Context context = getContext();
        memberAvatar.setMemberId(profile.getUserId());
        if (TextUtils.isEmpty(profile.getName())) {
            memberName.setText(memberImageHelper().getName(profile.getUserId(), isThreadOwner()));
        } else {
            memberName.setText(profile.getName());
        }
        memberRole.setText(RoleHelper.getRoleName(profile.getRole()));
        memberId.setText(getString(R.string.member_id, profile.getUserId()));
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
        detailsContainer.addView(DetailsItem_.build(context)
                .setDetails(getString(R.string.apps_uploaded), String.valueOf(profile.getFilesCount()))
                .setClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUserFiles();
                    }
                }));
        detailsContainer.addView(DetailsItem_.build(context)
                .setDetails(getString(R.string.messages_wrote), String.valueOf(profile.getMsgCount())));
        detailsContainer.addView(DetailsItem_.build(context)
                .setDetails(getString(R.string.apps_rated), String.valueOf(profile.getRatingsCount())));
        detailsContainer.addView(DetailsItem_.build(context)
                .setDetails(getString(R.string.moderators_assigned), String.valueOf(profile.getModeratorsCount())));
        boolean canChangeRole = false;
        boolean isPublicProfile = session.getUserData().getUserId() != profile.getUserId();
        if (isPublicProfile && grantRoles.length > 0) {
            for (int role : grantRoles) {
                if (role != profile.getRole()) {
                    canChangeRole = true;
                    break;
                }
            }
        }
        changeRoleButton.setVisibility(canChangeRole ? View.VISIBLE : View.GONE);
        authButton.setVisibility(isPublicProfile || profile.isRegistered() ? View.GONE : View.VISIBLE);
        showContent();
        swipeRefresh.setRefreshing(false);
    }

    private void showUserFiles() {
        FilesActivity_.intent(this).userId((long) profile.getUserId()).start();
    }

    private boolean isThreadOwner() {
        return profile.getUserId() == 1;
    }

    private void showError() {
        errorText.setText(R.string.profile_error);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadProfile();
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
