package com.tomclaw.appsend.core

import com.tomclaw.appsend.di.legacy.LegacyInjector
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Created by solkin on 23.09.17.
 */
class StoreServiceHolder private constructor() {

    private var injector: LegacyInjector? = null
    private var service: StoreService? = null

    private fun init() {
        this.injector = LegacyInjector.getInstance()
    }

    private fun lazyInit(): StoreService {
        val injector = injector ?: throw Exception("LegacyInjector is not initialized yet")
        return service ?: run {
            Retrofit.Builder()
                .client(injector.httpClient)
                .baseUrl("$HOST_URL/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StoreService::class.java).apply {
                    service = this
                }
        }
    }

    fun getService(): StoreService {
        return lazyInit()
    }

    companion object {
        private var instance: StoreServiceHolder? = null

        @JvmStatic
        fun getInstance(): StoreServiceHolder {
            return instance ?: run {
                StoreServiceHolder().apply { init() }
            }
        }
    }

}
