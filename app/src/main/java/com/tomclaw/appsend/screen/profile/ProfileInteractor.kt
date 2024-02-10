package com.tomclaw.appsend.screen.profile

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.SchedulersFactory

interface ProfileInteractor {
}

class ProfileInteractorImpl(
    api: StoreApi,
    schedulers: SchedulersFactory
) : ProfileInteractor {

}
