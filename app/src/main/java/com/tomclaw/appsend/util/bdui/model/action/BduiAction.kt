package com.tomclaw.appsend.util.bdui.model.action

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.bdui.model.transform.BduiTransform

/**
 * Base sealed interface for all BDUI actions.
 * Actions define behavior triggered by user interactions or server responses.
 */
sealed interface BduiAction {
    val type: String
}

/**
 * RPC action - sends a request to the server.
 * Server response contains a single action to execute.
 */
data class BduiRpcAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("endpoint")
    val endpoint: String,
    @SerializedName("method")
    val method: String = "POST",
    @SerializedName("payload")
    val payload: Any? = null
) : BduiAction {
    companion object {
        const val TYPE = "rpc"
    }
}

/**
 * Callback action - notifies the View layer about an event.
 * The host Activity/Fragment handles this callback.
 */
data class BduiCallbackAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("name")
    val name: String,
    @SerializedName("data")
    val data: Any? = null
) : BduiAction {
    companion object {
        const val TYPE = "callback"
    }
}

/**
 * Transform action - applies a single transform to the UI.
 */
data class BduiTransformAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("transform")
    val transform: BduiTransform
) : BduiAction {
    companion object {
        const val TYPE = "transform"
    }
}

/**
 * Sequence action - executes multiple actions sequentially.
 * This is the composite pattern for combining actions.
 */
data class BduiSequenceAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("actions")
    val actions: List<BduiAction>
) : BduiAction {
    companion object {
        const val TYPE = "sequence"
    }
}

/**
 * Route action - navigates to a screen within the app.
 * The host Activity handles the actual navigation.
 */
data class BduiRouteAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("screen")
    val screen: String,
    @SerializedName("params")
    val params: Map<String, Any>? = null
) : BduiAction {
    companion object {
        const val TYPE = "route"
    }
}

/**
 * Response from RPC request containing a single action to execute.
 */
data class BduiRpcResponse(
    @SerializedName("action")
    val action: BduiAction
)

