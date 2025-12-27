package com.tomclaw.appsend.util.bdui

import android.graphics.Color
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.ViewFlipper
import com.google.android.material.chip.Chip
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout
import com.tomclaw.appsend.util.bdui.model.transform.BduiBatchTransform
import com.tomclaw.appsend.util.bdui.model.transform.BduiPropertyTransform
import com.tomclaw.appsend.util.bdui.model.transform.BduiTransform

/**
 * Handles applying transforms to BDUI components.
 * Supports both regular View components and hidden value storage.
 */
class BduiTransformHandler(
    private val viewRegistry: BduiViewRegistry,
    private val hiddenStorage: BduiHiddenStorage
) {

    /**
     * Callback for executing actions triggered by hidden component transforms.
     * Set this after ActionHandler is created to enable reactive hidden components.
     */
    var onHiddenAction: ((com.tomclaw.appsend.util.bdui.model.action.BduiAction) -> Unit)? = null

    /**
     * Applies a transform to the UI.
     * Handles both single property transforms and batch transforms.
     */
    fun apply(transform: BduiTransform) {
        when (transform) {
            is BduiPropertyTransform -> applyPropertyTransform(transform)
            is BduiBatchTransform -> applyBatchTransform(transform)
        }
    }

    private fun applyPropertyTransform(transform: BduiPropertyTransform) {
        val id = transform.id
        val property = transform.property
        val value = transform.value

        // First check if it's a hidden value
        if (hiddenStorage.hasHidden(id)) {
            if (property == "value") {
                hiddenStorage.setHiddenValue(id, value)
            }
            // Trigger action if hidden has one (reactive hidden)
            hiddenStorage.getHiddenAction(id)?.let { action ->
                onHiddenAction?.invoke(action)
            }
            return
        }

        // Otherwise find the view and apply transform
        val view = viewRegistry.findViewById(id) ?: return
        applyPropertyToView(view, property, value)
    }

    private fun applyBatchTransform(transform: BduiBatchTransform) {
        transform.transforms.forEach { apply(it) }
    }

    private fun applyPropertyToView(view: View, property: String, value: Any) {
        when (property) {
            "text" -> applyText(view, value)
            "visibility" -> applyVisibility(view, value)
            "enabled" -> applyEnabled(view, value)
            "alpha" -> applyAlpha(view, value)
            "checked" -> applyChecked(view, value)
            "error" -> applyError(view, value)
            "progress" -> applyProgress(view, value)
            "rating" -> applyRating(view, value)
            "value" -> applyValue(view, value)
            "src" -> applySrc(view, value)
            "tint" -> applyTint(view, value)
            "hint" -> applyHint(view, value)
            "helperText" -> applyHelperText(view, value)
            "displayedChild" -> applyDisplayedChild(view, value)
            "autoStart" -> applyAutoStart(view, value)
            "flipInterval" -> applyFlipInterval(view, value)
        }
    }

    private fun applyText(view: View, value: Any) {
        when (view) {
            is TextView -> view.text = value.toString()
        }
    }

    private fun applyVisibility(view: View, value: Any) {
        view.visibility = when (value.toString().lowercase()) {
            "visible" -> View.VISIBLE
            "invisible" -> View.INVISIBLE
            "gone" -> View.GONE
            else -> View.VISIBLE
        }
    }

    private fun applyEnabled(view: View, value: Any) {
        view.isEnabled = when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> true
        }
    }

    private fun applyAlpha(view: View, value: Any) {
        view.alpha = when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 1f
            else -> 1f
        }
    }

    private fun applyChecked(view: View, value: Any) {
        val checked = when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> false
        }
        when (view) {
            is CompoundButton -> view.isChecked = checked
            is Chip -> view.isChecked = checked
        }
    }

    private fun applyError(view: View, value: Any) {
        when (view) {
            is TextInputLayout -> {
                val errorText = value.toString()
                view.error = if (errorText.isEmpty()) null else errorText
            }
        }
    }

    private fun applyProgress(view: View, value: Any) {
        val progress = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
        when (view) {
            is ProgressBar -> view.progress = progress
            is BaseProgressIndicator<*> -> view.progress = progress
        }
    }

    private fun applyRating(view: View, value: Any) {
        val rating = when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0f
            else -> 0f
        }
        when (view) {
            is RatingBar -> view.rating = rating
        }
    }

    private fun applyValue(view: View, value: Any) {
        val floatValue = when (value) {
            is Number -> value.toFloat()
            is String -> value.toFloatOrNull() ?: 0f
            else -> 0f
        }
        when (view) {
            is Slider -> view.value = floatValue.coerceIn(view.valueFrom, view.valueTo)
        }
    }

    private fun applySrc(view: View, value: Any) {
        // Image loading would be handled by the image loader
        // This is a placeholder for the actual implementation
        when (view) {
            is ImageView -> {
                // URL or resource loading should be delegated to image loader
            }
        }
    }

    private fun applyTint(view: View, value: Any) {
        val colorString = value.toString()
        try {
            val color = Color.parseColor(colorString)
            when (view) {
                is ImageView -> view.setColorFilter(color)
            }
        } catch (e: IllegalArgumentException) {
            // Invalid color format, ignore
        }
    }

    private fun applyHint(view: View, value: Any) {
        when (view) {
            is TextInputLayout -> view.hint = value.toString()
            is TextView -> view.hint = value.toString()
        }
    }

    private fun applyHelperText(view: View, value: Any) {
        when (view) {
            is TextInputLayout -> view.helperText = value.toString()
        }
    }

    private fun applyDisplayedChild(view: View, value: Any) {
        val index = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
        when (view) {
            is ViewFlipper -> {
                if (index >= 0 && index < view.childCount) {
                    view.displayedChild = index
                }
            }
        }
    }

    private fun applyAutoStart(view: View, value: Any) {
        val autoStart = when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> false
        }
        when (view) {
            is ViewFlipper -> {
                if (autoStart) {
                    view.startFlipping()
                } else {
                    view.stopFlipping()
                }
            }
        }
    }

    private fun applyFlipInterval(view: View, value: Any) {
        val interval = when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 3000
            else -> 3000
        }
        when (view) {
            is ViewFlipper -> view.flipInterval = interval
        }
    }
}

/**
 * Registry for accessing rendered views by their IDs.
 */
interface BduiViewRegistry {
    fun findViewById(id: String): View?
    fun registerView(id: String, view: View)
    fun unregisterView(id: String)
    fun clear()
}

/**
 * Storage for hidden component values and their actions.
 */
interface BduiHiddenStorage {
    fun hasHidden(id: String): Boolean
    fun getHiddenValue(id: String): Any?
    fun setHiddenValue(id: String, value: Any?)
    fun removeHidden(id: String)
    fun clear()

    // Action support for reactive hidden components
    fun registerHidden(id: String, value: Any?, action: com.tomclaw.appsend.util.bdui.model.action.BduiAction?)
    fun getHiddenAction(id: String): com.tomclaw.appsend.util.bdui.model.action.BduiAction?
}

