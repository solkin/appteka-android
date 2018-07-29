package com.tomclaw.appsend.main.local;

import com.flurry.android.FlurryAgent;
import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.AppItem;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.local_apps_fragment)
public class SelectInstalledFragment extends InstalledFragment {

    private CommonItemClickListener listener;

    public SelectInstalledFragment withListener(CommonItemClickListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onClick(final AppItem item) {
        FlurryAgent.logEvent("Select screen: installed");
        if (listener != null) {
            listener.onClick(item);
        }
    }
}
