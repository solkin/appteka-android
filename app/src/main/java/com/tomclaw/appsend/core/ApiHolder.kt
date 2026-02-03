package com.tomclaw.appsend.core

import com.google.gson.Gson
import com.tomclaw.appsend.screen.feed.api.PostDeserializer
import com.tomclaw.appsend.screen.feed.api.PostEntity
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiHolder(
    httpClientHolder: HttpClientHolder,
    gson: Gson
) {

    val storeApi: StoreApi = Retrofit.Builder()
        .client(httpClientHolder.getClient())
        .baseUrl("$HOST_URL/api/")
        .addConverterFactory(
            GsonConverterFactory.create(
                gson.newBuilder()
                    .registerTypeAdapter(PostEntity::class.java, PostDeserializer(gson))
                    .create()
            )
        )
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(StoreApi::class.java)

    val standByApi: StandByApi = Retrofit.Builder()
        .client(httpClientHolder.getClient())
        .baseUrl("$STAND_BY_HOST_URL/api/appteka/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .build()
        .create(StandByApi::class.java)

}
