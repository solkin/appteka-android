package com.tomclaw.appsend.util.bdui.model

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

/**
 * Layout parameters for positioning and sizing BDUI nodes.
 */
@GsonModel
data class BduiLayoutParams(
    @SerializedName("width")
    val width: String? = null,              // "match_parent", "wrap_content", or "100dp"
    @SerializedName("height")
    val height: String? = null,
    @SerializedName("weight")
    val weight: Float? = null,              // Layout weight for LinearLayout
    @SerializedName("gravity")
    val gravity: String? = null,            // Content gravity: "center", "start", "end", etc.
    @SerializedName("layoutGravity")
    val layoutGravity: String? = null,      // Layout gravity in parent
    @SerializedName("margin")
    val margin: BduiInsets? = null,
    @SerializedName("padding")
    val padding: BduiInsets? = null,
    @SerializedName("visibility")
    val visibility: String? = null,         // "visible", "invisible", "gone"
    @SerializedName("alpha")
    val alpha: Float? = null,
    @SerializedName("enabled")
    val enabled: Boolean? = null,
    @SerializedName("clickable")
    val clickable: Boolean? = null,
    @SerializedName("minWidth")
    val minWidth: String? = null,
    @SerializedName("minHeight")
    val minHeight: String? = null,
    @SerializedName("maxWidth")
    val maxWidth: String? = null,
    @SerializedName("maxHeight")
    val maxHeight: String? = null
)

/**
 * Insets for margin and padding.
 * Supports individual sides, horizontal/vertical shortcuts, or uniform "all".
 */
@GsonModel
data class BduiInsets(
    @SerializedName("left")
    val left: Int? = null,
    @SerializedName("top")
    val top: Int? = null,
    @SerializedName("right")
    val right: Int? = null,
    @SerializedName("bottom")
    val bottom: Int? = null,
    @SerializedName("start")
    val start: Int? = null,
    @SerializedName("end")
    val end: Int? = null,
    @SerializedName("horizontal")
    val horizontal: Int? = null,
    @SerializedName("vertical")
    val vertical: Int? = null,
    @SerializedName("all")
    val all: Int? = null
) {
    fun getLeft(): Int = start ?: left ?: horizontal ?: all ?: 0
    fun getTop(): Int = top ?: vertical ?: all ?: 0
    fun getRight(): Int = end ?: right ?: horizontal ?: all ?: 0
    fun getBottom(): Int = bottom ?: vertical ?: all ?: 0
}

