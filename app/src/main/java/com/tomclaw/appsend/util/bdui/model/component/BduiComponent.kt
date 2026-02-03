package com.tomclaw.appsend.util.bdui.model.component

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import com.tomclaw.appsend.util.bdui.model.BduiBackgroundStyle
import com.tomclaw.appsend.util.bdui.model.BduiImageStyle
import com.tomclaw.appsend.util.bdui.model.BduiLayoutParams
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.action.BduiAction

/**
 * Base sealed interface for all BDUI components.
 * Components are leaf nodes that render actual UI elements.
 */
sealed interface BduiComponent : BduiNode

// ============================================================================
// Hidden Component - stores data without rendering
// ============================================================================

/**
 * Hidden component - stores arbitrary data without rendering.
 * Values can be accessed via refs and modified via transforms.
 */
@GsonModel
data class BduiHiddenComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("value")
    val value: Any? = null
) : BduiComponent {
    companion object {
        const val TYPE = "hidden"
    }
}

// ============================================================================
// Text Components
// ============================================================================

/**
 * Text component - displays text content.
 */
@GsonModel
data class BduiTextComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("textSize")
    val textSize: Int? = null,              // Text size in sp
    @SerializedName("textColor")
    val textColor: String? = null,          // "#RRGGBB" or "#AARRGGBB"
    @SerializedName("textStyle")
    val textStyle: String? = null,          // "normal", "bold", "italic", "bold|italic"
    @SerializedName("gravity")
    val gravity: String? = null,            // "start", "center", "end", "center_horizontal"
    @SerializedName("maxLines")
    val maxLines: Int? = null,
    @SerializedName("lineHeight")
    val lineHeight: Int? = null,            // Line height in sp
    @SerializedName("letterSpacing")
    val letterSpacing: Float? = null,       // Letter spacing in em
    @SerializedName("selectable")
    val selectable: Boolean = false,
    @SerializedName("autoLink")
    val autoLink: Boolean = false
) : BduiComponent {
    companion object {
        const val TYPE = "text"
    }
}

// ============================================================================
// Button Components
// ============================================================================

/**
 * Button component - Material button with various styles.
 */
@GsonModel
data class BduiButtonComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("variant")
    val variant: String = "primary",        // "primary", "secondary", "outlined", "elevated", "text"
    @SerializedName("icon")
    val icon: String? = null,               // Icon resource name
    @SerializedName("iconGravity")
    val iconGravity: String? = null,        // "start", "end", "top", "textStart", "textEnd"
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "button"
    }
}

/**
 * Icon Button component - circular button with icon.
 */
@GsonModel
data class BduiIconButtonComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("variant")
    val variant: String = "standard",       // "standard", "filled", "tonal", "outlined"
    @SerializedName("contentDescription")
    val contentDescription: String? = null,
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "icon_button"
    }
}

/**
 * Floating Action Button component.
 */
@GsonModel
data class BduiFabComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("text")
    val text: String? = null,               // For extended FAB
    @SerializedName("size")
    val size: String = "normal",            // "mini", "normal", "large"
    @SerializedName("extended")
    val extended: Boolean = false,
    @SerializedName("contentDescription")
    val contentDescription: String? = null
) : BduiComponent {
    companion object {
        const val TYPE = "fab"
    }
}

// ============================================================================
// Image Components
// ============================================================================

/**
 * Image component - displays images from URL or resource.
 */
@GsonModel
data class BduiImageComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("src")
    val src: String? = null,                // URL or "res:icon_name"
    @SerializedName("placeholder")
    val placeholder: String? = null,
    @SerializedName("imageStyle")
    val imageStyle: BduiImageStyle? = null,
    @SerializedName("contentDescription")
    val contentDescription: String? = null
) : BduiComponent {
    companion object {
        const val TYPE = "image"
    }
}

/**
 * Icon component - displays vector icon.
 */
@GsonModel
data class BduiIconComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("icon")
    val icon: String,                       // Icon resource name
    @SerializedName("size")
    val size: Int = 24,                     // Size in dp
    @SerializedName("tint")
    val tint: String? = null,               // Tint color
    @SerializedName("contentDescription")
    val contentDescription: String? = null
) : BduiComponent {
    companion object {
        const val TYPE = "icon"
    }
}

// ============================================================================
// Input Components
// ============================================================================

/**
 * Text Input component - Material text field.
 */
@GsonModel
data class BduiInputComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("hint")
    val hint: String? = null,
    @SerializedName("helperText")
    val helperText: String? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("variant")
    val variant: String = "outlined",       // "outlined", "filled"
    @SerializedName("inputType")
    val inputType: String = "text",         // "text", "number", "email", "password", "phone", "multiline"
    @SerializedName("maxLines")
    val maxLines: Int = 1,
    @SerializedName("maxLength")
    val maxLength: Int? = null,
    @SerializedName("startIcon")
    val startIcon: String? = null,
    @SerializedName("endIcon")
    val endIcon: String? = null,
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "input"
    }
}

// ============================================================================
// Selection Components
// ============================================================================

/**
 * Switch component - Material switch toggle.
 */
@GsonModel
data class BduiSwitchComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("checked")
    val checked: Boolean? = null,           // default: false
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "switch"
    }
}

/**
 * Checkbox component - Material checkbox.
 */
@GsonModel
data class BduiCheckboxComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("checked")
    val checked: Boolean? = null,           // default: false
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "checkbox"
    }
}

/**
 * Radio Button component.
 */
@GsonModel
data class BduiRadioComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("checked")
    val checked: Boolean? = null,           // default: false
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "radio"
    }
}

/**
 * Radio Group component - groups radio buttons.
 */
@GsonModel
data class BduiRadioGroupComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("orientation")
    val orientation: String = "vertical",
    @SerializedName("items")
    val items: List<BduiRadioItem>? = null,
    @SerializedName("selectedId")
    val selectedId: String? = null
) : BduiComponent {
    companion object {
        const val TYPE = "radio_group"
    }
}

@GsonModel
data class BduiRadioItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("text")
    val text: String
)

// ============================================================================
// Chip Components
// ============================================================================

/**
 * Chip component - Material chip.
 */
@GsonModel
data class BduiChipComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("text")
    val text: String,
    @SerializedName("variant")
    val variant: String = "assist",         // "assist", "filter", "input", "suggestion"
    @SerializedName("icon")
    val icon: String? = null,
    @SerializedName("checked")
    val checked: Boolean? = null,           // default: false
    @SerializedName("checkable")
    val checkable: Boolean? = null,         // default: false
    @SerializedName("closeIcon")
    val closeIcon: Boolean? = null,         // default: false
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "chip"
    }
}

/**
 * Chip Group component - groups chips.
 */
@GsonModel
data class BduiChipGroupComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("chips")
    val chips: List<BduiChipItem>? = null,
    @SerializedName("singleSelection")
    val singleSelection: Boolean = false,
    @SerializedName("singleLine")
    val singleLine: Boolean = false
) : BduiComponent {
    companion object {
        const val TYPE = "chip_group"
    }
}

@GsonModel
data class BduiChipItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("text")
    val text: String,
    @SerializedName("checked")
    val checked: Boolean? = null,           // default: false
    @SerializedName("icon")
    val icon: String? = null
)

// ============================================================================
// Progress Components
// ============================================================================

/**
 * Progress Indicator component.
 */
@GsonModel
data class BduiProgressComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("variant")
    val variant: String = "circular",       // "circular", "linear", "wavy"
    @SerializedName("indeterminate")
    val indeterminate: Boolean = true,
    @SerializedName("progress")
    val progress: Int = 0,
    @SerializedName("max")
    val max: Int = 100
) : BduiComponent {
    companion object {
        const val TYPE = "progress"
    }
}

// ============================================================================
// Slider Components
// ============================================================================

/**
 * Slider component - Material slider.
 */
@GsonModel
data class BduiSliderComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("value")
    val value: Float = 0f,
    @SerializedName("valueFrom")
    val valueFrom: Float = 0f,
    @SerializedName("valueTo")
    val valueTo: Float = 100f,
    @SerializedName("stepSize")
    val stepSize: Float? = null,            // default: 0
    @SerializedName("enabled")
    val enabled: Boolean? = null            // default: true
) : BduiComponent {
    companion object {
        const val TYPE = "slider"
    }
}

// ============================================================================
// Rating Component
// ============================================================================

/**
 * Rating Bar component.
 */
@GsonModel
data class BduiRatingComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("rating")
    val rating: Float = 0f,
    @SerializedName("numStars")
    val numStars: Int = 5,
    @SerializedName("stepSize")
    val stepSize: Float = 1f,
    @SerializedName("isIndicator")
    val isIndicator: Boolean = false
) : BduiComponent {
    companion object {
        const val TYPE = "rating"
    }
}

// ============================================================================
// Card Component
// ============================================================================

/**
 * Card component - Material card that can contain children.
 */
@GsonModel
data class BduiCardComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("children")
    val children: List<BduiNode>? = null,
    @SerializedName("variant")
    val variant: String = "elevated",       // "elevated", "filled", "outlined"
    @SerializedName("cornerRadius")
    val cornerRadius: Int = 16,             // in dp
    @SerializedName("elevation")
    val elevation: Int = 1                  // in dp
) : BduiComponent {
    companion object {
        const val TYPE = "card"
    }
}

// ============================================================================
// Divider Component
// ============================================================================

/**
 * Divider component - Material divider line.
 */
@GsonModel
data class BduiDividerComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("orientation")
    val orientation: String = "horizontal", // "horizontal", "vertical"
    @SerializedName("insetStart")
    val insetStart: Int = 0,
    @SerializedName("insetEnd")
    val insetEnd: Int = 0,
    @SerializedName("thickness")
    val thickness: Int = 1                  // in dp
) : BduiComponent {
    companion object {
        const val TYPE = "divider"
    }
}

// ============================================================================
// Space Component
// ============================================================================

/**
 * Space component - invisible spacer.
 */
@GsonModel
data class BduiSpaceComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null
) : BduiComponent {
    companion object {
        const val TYPE = "space"
    }
}

// ============================================================================
// Toolbar Component
// ============================================================================

/**
 * Toolbar component - Material toolbar with navigation, title, subtitle, and menu.
 */
@GsonModel
data class BduiToolbarComponent(
    @SerializedName("id")
    override val id: String,
    @SerializedName("type")
    override val type: String = TYPE,
    @SerializedName("layoutParams")
    override val layoutParams: BduiLayoutParams? = null,
    @SerializedName("action")
    override val action: BduiAction? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("navigationIcon")
    val navigationIcon: String? = null,         // Icon resource name (e.g., "ic_arrow_back")
    @SerializedName("navigationAction")
    val navigationAction: BduiAction? = null,   // Action when navigation icon clicked
    @SerializedName("menu")
    val menu: List<BduiMenuItem>? = null,       // Menu items
    @SerializedName("titleCentered")
    val titleCentered: Boolean = false,
    @SerializedName("subtitleCentered")
    val subtitleCentered: Boolean = false,
    @SerializedName("elevation")
    val elevation: Int = 0,                     // Elevation in dp
    @SerializedName("backgroundColor")
    val backgroundColor: String? = null,        // Background color
    @SerializedName("titleTextColor")
    val titleTextColor: String? = null,
    @SerializedName("subtitleTextColor")
    val subtitleTextColor: String? = null,
    @SerializedName("navigationIconTint")
    val navigationIconTint: String? = null,
    @SerializedName("logo")
    val logo: String? = null,                   // Logo icon resource or URL
    @SerializedName("collapseIcon")
    val collapseIcon: String? = null,           // Icon for collapse action
    @SerializedName("contentInsetStart")
    val contentInsetStart: Int? = null,         // Content inset in dp
    @SerializedName("contentInsetEnd")
    val contentInsetEnd: Int? = null
) : BduiComponent {
    companion object {
        const val TYPE = "toolbar"
    }
}

/**
 * Menu item for toolbar.
 */
@GsonModel
data class BduiMenuItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("icon")
    val icon: String? = null,                   // Icon resource name
    @SerializedName("showAsAction")
    val showAsAction: String? = null,           // "never", "ifRoom", "always", "withText", "collapseActionView" (default: "ifRoom")
    @SerializedName("enabled")
    val enabled: Boolean? = null,               // default: true
    @SerializedName("visible")
    val visible: Boolean? = null,               // default: true
    @SerializedName("action")
    val action: BduiAction? = null,             // Action when menu item clicked
    @SerializedName("iconTint")
    val iconTint: String? = null
)

