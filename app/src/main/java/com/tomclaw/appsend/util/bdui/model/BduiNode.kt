package com.tomclaw.appsend.util.bdui.model

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import com.tomclaw.appsend.util.bdui.model.action.BduiAction

/**
 * Base interface for all BDUI nodes (containers and components)
 */
interface BduiNode {
    val id: String
    val type: String
    val layoutParams: BduiLayoutParams?
    val action: BduiAction?
}

/**
 * Reference to a property value of another component.
 * Used for dynamic data binding in actions and transforms.
 */
@GsonModel
data class BduiRef(
    @SerializedName("type")
    val type: String = "ref",
    @SerializedName("id")
    val id: String,
    @SerializedName("property")
    val property: String
)

