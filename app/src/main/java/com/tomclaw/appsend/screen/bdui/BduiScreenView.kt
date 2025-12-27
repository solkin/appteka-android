package com.tomclaw.appsend.screen.bdui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.ViewFlipper
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
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

    fun routeEvents(): Observable<BduiRouteEvent>

    fun openUrlEvents(): Observable<BduiOpenUrlEvent>

    fun shareEvents(): Observable<BduiShareEvent>

    fun reloadEvents(): Observable<Unit>

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

data class BduiRouteEvent(
    val screen: String,
    val params: Map<String, Any>?
)

data class BduiOpenUrlEvent(
    val url: String,
    val external: Boolean
)

data class BduiShareEvent(
    val text: String,
    val title: String?
)

class BduiScreenViewImpl(
    view: View,
    schedulersFactory: SchedulersFactory,
    preferencesStorage: com.tomclaw.appsend.util.bdui.BduiPreferencesStorage
) : BduiScreenView, BduiActionListener {

    private val context: Context = view.context
    private val toolbarContainer: AppBarLayout = view.findViewById(R.id.toolbar_container)
    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val viewFlipper: ViewFlipper = view.findViewById(R.id.view_flipper)
    private val bduiView: BduiView = view.findViewById(R.id.bdui_view)
    private val retryButton: MaterialButton = view.findViewById(R.id.retry_button)

    private val actionBarSize: Int

    private val navigationRelay = PublishRelay.create<Unit>()
    private val retryRelay = PublishRelay.create<Unit>()
    private val callbackRelay = PublishRelay.create<BduiCallbackEvent>()
    private val rpcRelay = PublishRelay.create<BduiRpcRequest>()
    private val routeRelay = PublishRelay.create<BduiRouteEvent>()
    private val openUrlRelay = PublishRelay.create<BduiOpenUrlEvent>()
    private val shareRelay = PublishRelay.create<BduiShareEvent>()
    private val reloadRelay = PublishRelay.create<Unit>()

    init {
        val typedArray = view.context.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        actionBarSize = typedArray.getDimensionPixelSize(0, 0)
        typedArray.recycle()

        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
        retryButton.setOnClickListener { retryRelay.accept(Unit) }

        bduiView.initialize(schedulersFactory, preferencesStorage, this)
    }

    override fun showLoading() {
        setToolbarVisible(true)
        viewFlipper.displayedChild = VIEW_LOADING
    }

    override fun showContent(schema: BduiNode) {
        setToolbarVisible(false)
        bduiView.render(schema)
        viewFlipper.displayedChild = VIEW_CONTENT
    }

    override fun showError() {
        setToolbarVisible(true)
        viewFlipper.displayedChild = VIEW_ERROR
    }

    private fun setToolbarVisible(visible: Boolean) {
        toolbarContainer.visibility = if (visible) View.VISIBLE else View.GONE
        val layoutParams = viewFlipper.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.topMargin = if (visible) actionBarSize else 0
        viewFlipper.layoutParams = layoutParams
    }

    override fun setTitle(title: String?) {
        toolbar.title = title ?: ""
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun retryClicks(): Observable<Unit> = retryRelay

    override fun callbackEvents(): Observable<BduiCallbackEvent> = callbackRelay

    override fun rpcRequests(): Observable<BduiRpcRequest> = rpcRelay

    override fun routeEvents(): Observable<BduiRouteEvent> = routeRelay

    override fun openUrlEvents(): Observable<BduiOpenUrlEvent> = openUrlRelay

    override fun shareEvents(): Observable<BduiShareEvent> = shareRelay

    override fun reloadEvents(): Observable<Unit> = reloadRelay

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

    override fun onRoute(screen: String, params: Map<String, Any>?) {
        routeRelay.accept(BduiRouteEvent(screen, params))
    }

    override fun onOpenUrl(url: String, external: Boolean) {
        openUrlRelay.accept(BduiOpenUrlEvent(url, external))
    }

    override fun onSnackbar(message: String, duration: String, actionText: String?, onAction: () -> Unit) {
        val snackbarDuration = when (duration) {
            "long" -> Snackbar.LENGTH_LONG
            "indefinite" -> Snackbar.LENGTH_INDEFINITE
            else -> Snackbar.LENGTH_SHORT
        }
        val snackbar = Snackbar.make(bduiView, message, snackbarDuration)
        actionText?.let {
            snackbar.setAction(it) { onAction() }
        }
        snackbar.show()
    }

    override fun onCopy(text: String, label: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onShare(text: String, title: String?) {
        shareRelay.accept(BduiShareEvent(text, title))
    }

    override fun onReload() {
        reloadRelay.accept(Unit)
    }

    override fun onFocus(id: String, showKeyboard: Boolean) {
        val targetView = bduiView.findBduiViewById(id) ?: return
        targetView.requestFocus()
        if (showKeyboard) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(targetView, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onScrollTo(id: String, smooth: Boolean) {
        val targetView = bduiView.findBduiViewById(id) ?: return
        val scrollParent = findScrollParent(targetView)
        scrollParent?.let { scroll ->
            val location = IntArray(2)
            targetView.getLocationInWindow(location)
            val scrollLocation = IntArray(2)
            scroll.getLocationInWindow(scrollLocation)
            val scrollY = location[1] - scrollLocation[1]
            
            if (smooth) {
                when (scroll) {
                    is NestedScrollView -> scroll.smoothScrollTo(0, scrollY)
                    is ScrollView -> scroll.smoothScrollTo(0, scrollY)
                }
            } else {
                when (scroll) {
                    is NestedScrollView -> scroll.scrollTo(0, scrollY)
                    is ScrollView -> scroll.scrollTo(0, scrollY)
                }
            }
        }
    }

    private fun findScrollParent(view: View): ViewGroup? {
        var parent = view.parent
        while (parent != null) {
            if (parent is NestedScrollView || parent is ScrollView) {
                return parent as ViewGroup
            }
            parent = parent.parent
        }
        return null
    }

    companion object {
        private const val VIEW_LOADING = 0
        private const val VIEW_CONTENT = 1
        private const val VIEW_ERROR = 2
    }
}

