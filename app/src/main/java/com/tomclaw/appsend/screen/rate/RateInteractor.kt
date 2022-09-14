package com.tomclaw.appsend.screen.rate

import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory

interface RateInteractor {
}

class RateInteractorImpl(
    private val api: StoreApi,
    private val userDataInteractor: UserDataInteractor,
    private val schedulers: SchedulersFactory
) : RateInteractor {
}
