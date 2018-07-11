package com.tomclaw.appsend.main.store;

import android.support.annotation.NonNull;

import com.tomclaw.appsend.BuildConfig;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.UserData;
import com.tomclaw.appsend.net.UserDataListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import retrofit2.Call;

@EFragment(R.layout.store_fragment)
public class UserUploadsFragment extends BaseStoreFragment implements UserDataListener {

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @InstanceState
    Long userId;

    @Override
    public Call<ListResponse> createCall(String appId) {
        if (userId == null) return null;
        int build = BuildConfig.VERSION_CODE;
        return serviceHolder.getService().listFiles(1, userId, appId, null, build);
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getInstance().getUserHolder().attachListener(this);
    }

    @Override
    public void onStop() {
        Session.getInstance().getUserHolder().removeListener(this);
        super.onStop();
    }

    @Override
    public void onUserDataChanged(@NonNull final UserData userData) {
        MainExecutor.execute(new Runnable() {
            @Override
            public void run() {
                setUserData(userData);
            }
        });
    }

    private void setUserData(UserData userData) {
        if (userId == null || userId != userData.getUserId()) {
            userId = userData.getUserId();
            loadFiles(true);
        }
    }
}
