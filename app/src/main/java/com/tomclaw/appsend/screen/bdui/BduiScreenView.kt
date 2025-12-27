package com.tomclaw.appsend.screen.bdui

import android.view.View
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButton
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.bdui.BduiActionListener
import com.tomclaw.appsend.util.bdui.BduiView
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcResponse
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface BduiScreenView {

    fun showLoading()

    fun showContent(schema: BduiNode)

    fun showError()

    fun setTitle(title: String?)

    fun navigationClicks(): Observable<Unit>

    fun retryClicks(): Observable<Unit>

    fun callbackEvents(): Observable<BduiCallbackEvent>

    fun rpcRequests(): Observable<BduiRpcRequest>

}

data class BduiCallbackEvent(
    val name: String,
    val data: Any?
)

data class BduiRpcRequest(
    val action: BduiRpcAction,
    val responseEmitter: (BduiRpcResponse) -> Unit,
    val errorEmitter: (Throwable) -> Unit
)

class BduiScreenViewImpl(
    view: View,
    private val schedulersFactory: SchedulersFactory
) : BduiScreenView, BduiActionListener {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val viewFlipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val bduiView: BduiView = view.findViewById(R.id.bdui_view)
    private val retryButton: MaterialButton = view.findViewById(R.id.retry_button)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val callbackRelay = PublishRelay.create<BduiCallbackEvent>()
    private val rpcRelay = PublishRelay.create<BduiRpcRequest>()

    init {
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        retryButton.setOnClickListener { retryRelay.accept(Unit) }

        bduiView.initialize(schedulersFactory, this)
    }

    override fun showLoading() {
        viewFlipper.displayedChild = VIEW_LOADING
    }

    override fun showContent(schema: BduiNode) {
        bduiView.render(schema)
        viewFlipper.displayedChild = VIEW_CONTENT
    }

    override fun showError() {
        viewFlipper.displayedChild = VIEW_ERROR
    }

    override fun setTitle(title: String?) {
        toolbar.title = title ?: ""
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun callbackEvents(): Observable<BduiCallbackEvent> = callbackRelay

    override fun rpcRequests(): Observable<BduiRpcRequest> = rpcRelay

    // BduiActionListener implementation

    override fun onCallback(name: String, data: Any?) {
        callbackRelay.accept(BduiCallbackEvent(name, data))
    }

    override fun onRpcRequest(action: BduiRpcAction): Single<BduiRpcResponse> {
        return Single.create { emitter ->
            val request = BduiRpcRequest(
                action = action,
                responseEmitter = { response ->
                    if (!emitter.isDisposed) {
                        emitter.onSuccess(response)
                    }
                },
                errorEmitter = { error ->
                    if (!emitter.isDisposed) {
                        emitter.onError(error)
                    }
                }
            )
            rpcRelay.accept(request)
        }
    }

    companion object {
        private const val VIEW_LOADING = 0
        private const val VIEW_CONTENT = 1
        private const val VIEW_ERROR = 2
    }
}

