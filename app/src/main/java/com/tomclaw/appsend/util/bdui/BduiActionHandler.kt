package com.tomclaw.appsend.util.bdui

import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.bdui.model.action.BduiAction
import com.tomclaw.appsend.util.bdui.model.action.BduiCallbackAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRouteAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcAction
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcResponse
import com.tomclaw.appsend.util.bdui.model.action.BduiSequenceAction
import com.tomclaw.appsend.util.bdui.model.action.BduiTransformAction
import com.tomclaw.appsend.util.bdui.model.transform.BduiBatchTransform
import com.tomclaw.appsend.util.bdui.model.transform.BduiPropertyTransform
import com.tomclaw.appsend.util.bdui.model.transform.BduiTransform
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Handles execution of BDUI actions.
 * Resolves refs, executes transforms, callbacks, and RPC requests.
 */
class BduiActionHandler(
    private val transformHandler: BduiTransformHandler,
    private val refResolver: BduiRefResolver,
    private val listener: BduiActionListener,
    private val schedulers: SchedulersFactory
) {

    /**
     * Executes a single action.
     * Refs are resolved before execution.
     */
    fun execute(action: BduiAction): Completable {
        val resolvedAction = resolveRefsInAction(action)
        return executeResolved(resolvedAction)
    }

    private fun executeResolved(action: BduiAction): Completable {
        return when (action) {
            is BduiRpcAction -> executeRpc(action)
            is BduiCallbackAction -> executeCallback(action)
            is BduiTransformAction -> executeTransform(action)
            is BduiSequenceAction -> executeSequence(action)
            is BduiRouteAction -> executeRoute(action)
        }
    }

    private fun executeRpc(action: BduiRpcAction): Completable {
        return listener.onRpcRequest(action)
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .flatMapCompletable { response ->
                execute(response.action)
            }
    }

    private fun executeCallback(action: BduiCallbackAction): Completable {
        return Completable.fromAction {
            listener.onCallback(action.name, action.data)
        }.subscribeOn(schedulers.mainThread())
    }

    private fun executeTransform(action: BduiTransformAction): Completable {
        return Completable.fromAction {
            transformHandler.apply(action.transform)
        }.subscribeOn(schedulers.mainThread())
    }

    private fun executeSequence(action: BduiSequenceAction): Completable {
        return Observable.fromIterable(action.actions)
            .concatMapCompletable { execute(it) }
    }

    private fun executeRoute(action: BduiRouteAction): Completable {
        return Completable.fromAction {
            listener.onRoute(action.screen, action.params)
        }.subscribeOn(schedulers.mainThread())
    }

    // ========================================================================
    // Ref Resolution
    // ========================================================================

    private fun resolveRefsInAction(action: BduiAction): BduiAction {
        return when (action) {
            is BduiRpcAction -> resolveRefsInRpcAction(action)
            is BduiCallbackAction -> resolveRefsInCallbackAction(action)
            is BduiTransformAction -> resolveRefsInTransformAction(action)
            is BduiSequenceAction -> resolveRefsInSequenceAction(action)
            is BduiRouteAction -> resolveRefsInRouteAction(action)
        }
    }

    private fun resolveRefsInRpcAction(action: BduiRpcAction): BduiRpcAction {
        return action.copy(
            payload = refResolver.resolve(action.payload)
        )
    }

    private fun resolveRefsInCallbackAction(action: BduiCallbackAction): BduiCallbackAction {
        return action.copy(
            data = refResolver.resolve(action.data)
        )
    }

    private fun resolveRefsInTransformAction(action: BduiTransformAction): BduiTransformAction {
        return action.copy(
            transform = resolveRefsInTransform(action.transform)
        )
    }

    private fun resolveRefsInSequenceAction(action: BduiSequenceAction): BduiSequenceAction {
        return action.copy(
            actions = action.actions.map { resolveRefsInAction(it) }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveRefsInRouteAction(action: BduiRouteAction): BduiRouteAction {
        return action.copy(
            params = refResolver.resolve(action.params) as? Map<String, Any>
        )
    }

    private fun resolveRefsInTransform(transform: BduiTransform): BduiTransform {
        return when (transform) {
            is BduiPropertyTransform -> transform.copy(
                value = refResolver.resolve(transform.value) ?: transform.value
            )
            is BduiBatchTransform -> transform.copy(
                transforms = transform.transforms.map { resolveRefsInTransform(it) }
            )
        }
    }
}

/**
 * Listener interface for handling BDUI action callbacks, RPC requests, and navigation.
 * Implemented by the host Activity/Fragment or Presenter.
 */
interface BduiActionListener {

    /**
     * Called when a callback action is executed.
     *
     * @param name Callback name for routing
     * @param data Optional data payload (with refs already resolved)
     */
    fun onCallback(name: String, data: Any?)

    /**
     * Called when an RPC action needs to be executed.
     * The implementation should make the network request and return the response.
     *
     * @param action RPC action with endpoint, method, and resolved payload
     * @return Single emitting the server response containing the next action
     */
    fun onRpcRequest(action: BduiRpcAction): Single<BduiRpcResponse>

    /**
     * Called when a route action is executed.
     * The implementation should navigate to the specified screen.
     *
     * @param screen Screen name/identifier
     * @param params Optional parameters for the target screen (with refs already resolved)
     */
    fun onRoute(screen: String, params: Map<String, Any>?)
}

