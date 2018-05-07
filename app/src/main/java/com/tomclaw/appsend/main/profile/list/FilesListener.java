package com.tomclaw.appsend.main.profile.list;

import com.tomclaw.appsend.main.item.StoreItem;

/**
 * Created by solkin on 03.08.17.
 */
public interface FilesListener {

    int STATE_LOADED = 0x01;
    int STATE_LOADING = 0x02;
    int STATE_FAILED = 0x03;

    int onNextPage();

    void onRetry();

    void onClick(StoreItem item);

}
