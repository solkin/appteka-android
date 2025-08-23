package com.tomclaw.appsend.main.local;

import com.tomclaw.appsend.main.item.ApkItem;

public class SelectDistroFragment extends DistroFragment {

    @Override
    public void loadAttempt() {
        invalidate();
        loadFiles();
    }

    @Override
    public void onClick(final ApkItem item) {
        CommonItemClickListener listener = null;
        if (getActivity() instanceof CommonItemClickListener) {
            listener = (CommonItemClickListener) getActivity();
        }
        if (listener != null) {
            listener.onClick(item);
        }
    }
}
