package com.tomclaw.appsend.main.local;

import com.tomclaw.appsend.main.item.AppItem;

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
