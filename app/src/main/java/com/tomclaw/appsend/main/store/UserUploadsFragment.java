package com.tomclaw.appsend.main.store;

import androidx.annotation.NonNull;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.core.MainExecutor;
import com.tomclaw.appsend.core.StoreServiceHolder;
import com.tomclaw.appsend.main.dto.ApiResponse;
import com.tomclaw.appsend.main.profile.list.ListResponse;
import com.tomclaw.appsend.net.Session;
import com.tomclaw.appsend.net.UserData;
import com.tomclaw.appsend.net.UserDataListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;

import retrofit2.Call;

import static com.tomclaw.appsend.util.LocaleHelper.getLocaleLanguage;

@EFragment(R.layout.uploads_fragment)
public class UserUploadsFragment extends BaseStoreFragment implements UserDataListener {

    @Bean
    StoreServiceHolder serviceHolder;

    @Bean
    Session session;

    @InstanceState
    Long userId;

    @Override
    public Call<ApiResponse<ListResponse>> createCall(String appId, int offset) {
        if (userId == null) return null;
        String locale = getLocaleLanguage();
        return serviceHolder.getService().listUserFiles(userId, session.getUserHolder().getUserData().getGuid(), appId, locale);
    }

    @Override
    public void onStart() {
        super.onStart();
        session.getUserHolder().attachListener(this);
    }

    @Override
    public void onStop() {
        session.getUserHolder().removeListener(this);
        super.onStop();
    }

    @Override
    public void onUserDataChanged(@NonNull final UserData userData) {
        MainExecutor.execute(() -> setUserId(userData.getUserId()));
    }

    public void setUserId(long userId) {
        if (this.userId == null || this.userId != userId) {
            this.userId = userId;
            showProgress();
            loadFiles(true);
        }
    }
}
