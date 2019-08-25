package com.tomclaw.appsend.main.local;

import com.tomclaw.appsend.R;
import com.tomclaw.appsend.main.item.AppItem;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.local_apps_fragment)
public class SelectInstalledFragment extends InstalledFragment {

    @Override
    public void onClick(final AppItem item) {
        CommonItemClickListener listener = null;
        if (getActivity() instanceof CommonItemClickListener) {
            listener = (CommonItemClickListener) getActivity();
        }
        if (listener != null) {
            listener.onClick(item);
        }
    }
}
