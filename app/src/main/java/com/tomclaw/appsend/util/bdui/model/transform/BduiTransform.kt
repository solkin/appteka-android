package com.tomclaw.appsend.util.bdui.model.transform

import com.google.gson.annotations.SerializedName

/**
 * Base sealed interface for all BDUI transforms.
 * Transforms modify properties of existing components.
 */
sealed interface BduiTransform {
    val type: String
}

/**
 * Property transform - changes a single property of a component.
 */
data class BduiPropertyTransform(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("id")
    val id: String,
    @SerializedName("property")
    val property: String,
    @SerializedName("value")
    val value: Any
) : BduiTransform {
    companion object {
        const val TYPE = "property"
    }
}

/**
 * Batch transform - applies multiple transforms at once.
 * This is the composite pattern for combining transforms.
 */
data class BduiBatchTransform(
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("transforms")
    val transforms: List<BduiTransform>
) : BduiTransform {
    companion object {
        const val TYPE = "batch"
    }
}

