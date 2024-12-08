package com.tomclaw.appsend.di.legacy

import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.core.MigrationManager
import com.tomclaw.appsend.core.StoreApi
import com.tomclaw.appsend.util.Analytics
import okhttp3.OkHttpClient
import javax.inject.Inject

class LegacyInjector {

    @Inject
    lateinit var api: StoreApi

    @Inject
    lateinit var httpClient: OkHttpClient

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var migration: MigrationManager

    fun init() {
        Appteka.getComponent().legacyComponent(LegacyModule()).inject(this)
    }

    companion object {
        private var instance: LegacyInjector? = null

        @JvmStatic
        fun getInstance(): LegacyInjector {
            return instance ?: run {
                LegacyInjector().apply { init() }
            }
        }
    }
}
