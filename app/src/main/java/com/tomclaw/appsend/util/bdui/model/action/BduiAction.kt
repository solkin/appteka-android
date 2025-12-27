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
 * Open URL action - opens a URL in browser or in-app WebView.
 */
data class BduiOpenUrlAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("url")
    val url: String,
    @SerializedName("external")
    val external: Boolean = true
) : BduiAction {
    companion object {
        const val TYPE = "open_url"
    }
}

/**
 * Snackbar action - shows a snackbar message.
 */
data class BduiSnackbarAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("message")
    val message: String,
    @SerializedName("duration")
    val duration: String = "short",
    @SerializedName("actionText")
    val actionText: String? = null,
    @SerializedName("action")
    val action: BduiAction? = null
) : BduiAction {
    companion object {
        const val TYPE = "snackbar"
    }
}

/**
 * Copy action - copies text to clipboard.
 */
data class BduiCopyAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("text")
    val text: String,
    @SerializedName("label")
    val label: String = "Copied text"
) : BduiAction {
    companion object {
        const val TYPE = "copy"
    }
}

/**
 * Share action - shares content via system share dialog.
 */
data class BduiShareAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("text")
    val text: String,
    @SerializedName("title")
    val title: String? = null
) : BduiAction {
    companion object {
        const val TYPE = "share"
    }
}

/**
 * Delay action - delays execution of the next action in a sequence.
 */
data class BduiDelayAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("duration")
    val duration: Long
) : BduiAction {
    companion object {
        const val TYPE = "delay"
    }
}

/**
 * Store action - saves a value to SharedPreferences.
 */
data class BduiStoreAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("key")
    val key: String,
    @SerializedName("value")
    val value: Any?
) : BduiAction {
    companion object {
        const val TYPE = "store"
    }
}

/**
 * Load action - loads a value from SharedPreferences into a hidden component.
 */
data class BduiLoadAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("key")
    val key: String,
    @SerializedName("targetId")
    val targetId: String,
    @SerializedName("defaultValue")
    val defaultValue: Any? = null
) : BduiAction {
    companion object {
        const val TYPE = "load"
    }
}

/**
 * Reload action - reloads the current BDUI screen.
 */
data class BduiReloadAction(
    @SerializedName("type")
    override val type: String = TYPE
) : BduiAction {
    companion object {
        const val TYPE = "reload"
    }
}

/**
 * Focus action - sets focus on a component.
 */
data class BduiFocusAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("id")
    val id: String,
    @SerializedName("showKeyboard")
    val showKeyboard: Boolean = true
) : BduiAction {
    companion object {
        const val TYPE = "focus"
    }
}

/**
 * Scroll to action - scrolls to a component.
 */
data class BduiScrollToAction(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("id")
    val id: String,
    @SerializedName("smooth")
    val smooth: Boolean = true
) : BduiAction {
    companion object {
        const val TYPE = "scroll_to"
    }
}

/**
 * Response from RPC request containing a single action to execute.
 */
data class BduiRpcResponse(
    @SerializedName("action")
    val action: BduiAction
)

