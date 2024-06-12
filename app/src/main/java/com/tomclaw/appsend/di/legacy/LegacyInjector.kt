package com.tomclaw.appsend.di.legacy

import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.Analytics
import okhttp3.OkHttpClient
import org.androidannotations.annotations.AfterInject
import org.androidannotations.annotations.EBean
import javax.inject.Inject

@EBean(scope = EBean.Scope.Singleton)
open class LegacyInjector {

    @Inject
    lateinit var api: StoreApi

    @Inject
    lateinit var httpClient: OkHttpClient

    @Inject
    lateinit var analytics: Analytics

    @AfterInject
    fun init() {
        Appteka.getComponent().legacyComponent(LegacyModule()).inject(this)
    }

}
