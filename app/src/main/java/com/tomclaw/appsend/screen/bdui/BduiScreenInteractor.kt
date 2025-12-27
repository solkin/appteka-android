package com.tomclaw.appsend.screen.bdui

import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcResponse
import com.tomclaw.appsend.util.bdui.parser.BduiJsonParser
import io.reactivex.rxjava3.core.Single
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

interface BduiScreenInteractor {

    fun loadSchema(url: String): Single<BduiNode>

    fun executeRpc(action: BduiRpcAction): Single<BduiRpcResponse>

}

class BduiScreenInteractorImpl(
    private val httpClient: OkHttpClient,
    private val schedulers: SchedulersFactory
) : BduiScreenInteractor {

    override fun loadSchema(url: String): Single<BduiNode> {
        return Single
            .create { emitter ->
                try {
                    val request = Request.Builder()
                        .url(url)
                        .get()
                        .build()

                    val response = httpClient.newCall(request).execute()

                    if (response.isSuccessful) {
                        val json = response.body?.string()
                            ?: throw IllegalStateException("Empty response body")
                        val node = BduiJsonParser.parseNode(json)
                        emitter.onSuccess(node)
                    } else {
                        emitter.onError(
                            IllegalStateException("Failed to load schema: ${response.code}")
                        )
                    }
                } catch (e: Exception) {
                    if (!emitter.isDisposed) {
                        emitter.onError(e)
                    }
                }
            }
            .subscribeOn(schedulers.io())
    }

    override fun executeRpc(action: BduiRpcAction): Single<BduiRpcResponse> {
        return Single
            .create { emitter ->
                try {
                    val requestBuilder = Request.Builder()
                        .url(action.endpoint)

                    when (action.method.uppercase()) {
                        "GET" -> requestBuilder.get()
                        "POST" -> {
                            val jsonPayload = action.payload?.let {
                                BduiJsonParser.gson.toJson(it)
                            } ?: "{}"
                            val body = jsonPayload.toRequestBody("application/json".toMediaType())
                            requestBuilder.post(body)
                        }
                        "PUT" -> {
                            val jsonPayload = action.payload?.let {
                                BduiJsonParser.gson.toJson(it)
                            } ?: "{}"
                            val body = jsonPayload.toRequestBody("application/json".toMediaType())
                            requestBuilder.put(body)
                        }
                        "DELETE" -> requestBuilder.delete()
                        else -> requestBuilder.get()
                    }

                    val response = httpClient.newCall(requestBuilder.build()).execute()

                    if (response.isSuccessful) {
                        val json = response.body?.string()
                            ?: throw IllegalStateException("Empty response body")
                        val rpcResponse = BduiJsonParser.parseRpcResponse(json)
                        emitter.onSuccess(rpcResponse)
                    } else {
                        emitter.onError(
                            IllegalStateException("RPC failed: ${response.code}")
                        )
                    }
                } catch (e: Exception) {
                    if (!emitter.isDisposed) {
                        emitter.onError(e)
                    }
                }
            }
            .subscribeOn(schedulers.io())
    }
}
