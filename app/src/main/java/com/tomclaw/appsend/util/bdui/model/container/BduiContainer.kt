package com.tomclaw.appsend.util.bdui.model.container

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import com.tomclaw.appsend.util.bdui.model.BduiLayoutParams
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.action.BduiAction

/**
 * Base sealed interface for all BDUI containers.
 * Containers hold and arrange child nodes.
 */
sealed interface BduiContainer : BduiNode {
    val children: List<BduiNode>?
}

/**
 * FrameLayout container - stacks children on top of each other.
 */
@GsonModel
data class BduiFrameContainer(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("children")
    override val children: List<BduiNode>? = null
) : BduiContainer {
    companion object {
        const val TYPE = "frame"
    }
}

/**
 * LinearLayout container - arranges children in a row or column.
 */
@GsonModel
data class BduiLinearContainer(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("children")
    override val children: List<BduiNode>? = null,
    @SerializedName("orientation")
    val orientation: String = "vertical",   // "vertical" or "horizontal"
    @SerializedName("gravity")
    val gravity: String? = null,            // Content gravity
    @SerializedName("weightSum")
    val weightSum: Float? = null,
    @SerializedName("divider")
    val divider: BduiDividerConfig? = null
) : BduiContainer {
    companion object {
        const val TYPE = "linear"
    }
}

/**
 * Divider configuration for LinearLayout.
 */
@GsonModel
data class BduiDividerConfig(
    @SerializedName("showDividers")
    val showDividers: String? = null,       // "none", "beginning", "middle", "end"
    @SerializedName("dividerPadding")
    val dividerPadding: Int? = null
)

/**
 * RecyclerView container - displays a scrollable list of items.
 */
@GsonModel
data class BduiRecyclerContainer(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("children")
    override val children: List<BduiNode>? = null,
    @SerializedName("orientation")
    val orientation: String = "vertical",   // "vertical" or "horizontal"
    @SerializedName("spanCount")
    val spanCount: Int = 1,                 // For GridLayoutManager
    @SerializedName("reverseLayout")
    val reverseLayout: Boolean = false,
    @SerializedName("itemSpacing")
    val itemSpacing: Int? = null            // Spacing between items in dp
) : BduiContainer {
    companion object {
        const val TYPE = "recycler"
    }
}

/**
 * ScrollView container - provides vertical scrolling for content.
 */
@GsonModel
data class BduiScrollContainer(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("children")
    override val children: List<BduiNode>? = null,
    @SerializedName("orientation")
    val orientation: String = "vertical",   // "vertical" or "horizontal"
    @SerializedName("fillViewport")
    val fillViewport: Boolean = false
) : BduiContainer {
    companion object {
        const val TYPE = "scroll"
    }
}

/**
 * ViewFlipper container - displays one child at a time, supports switching between children.
 * Can be controlled via transforms to change displayedChild.
 */
@GsonModel
data class BduiFlipperContainer(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("children")
    override val children: List<BduiNode>? = null,
    @SerializedName("displayedChild")
    val displayedChild: Int = 0,            // Index of the child to display
    @SerializedName("autoStart")
    val autoStart: Boolean = false,         // Auto-start flipping animation
    @SerializedName("flipInterval")
    val flipInterval: Int = 3000,           // Interval between flips in ms
    @SerializedName("inAnimation")
    val inAnimation: String? = null,        // "fade_in", "slide_in_left", "slide_in_right", etc.
    @SerializedName("outAnimation")
    val outAnimation: String? = null        // "fade_out", "slide_out_left", "slide_out_right", etc.
) : BduiContainer {
    companion object {
        const val TYPE = "flipper"
    }
}

