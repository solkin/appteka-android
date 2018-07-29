package com.tomclaw.appsend.main.local;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.ApkItem;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.local_apps_fragment)
public class SelectDistroFragment extends DistroFragment {

    private CommonItemClickListener listener;

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
