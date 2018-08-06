package com.tomclaw.appsend.main.local;

import android.Manifest;

import com.flurry.android.FlurryAgent;
import com.greysonparrelli.permiso.Permiso;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.ApkItem;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.local_apps_fragment)
public class SelectDistroFragment extends DistroFragment {

    private CommonItemClickListener listener;

    @Override
    public void loadAttempt() {
        Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
            @Override
            public void onPermissionResult(Permiso.ResultSet resultSet) {
                if (resultSet.areAllPermissionsGranted()) {
                    invalidate();
                    loadFiles();
                } else {
                    errorText.setText(R.string.write_permission_select);
                    showError();
                }
            }

            @Override
            public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                String title = getContext().getString(R.string.app_name);
                String message = getContext().getString(R.string.write_permission_select);
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback);
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public SelectDistroFragment withListener(CommonItemClickListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onClick(final ApkItem item) {
        FlurryAgent.logEvent("Select screen: distro");
        if (listener != null) {
            listener.onClick(item);
        }
    }
}
