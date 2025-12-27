package com.tomclaw.appsend.util.bdui

import android.view.View
import android.view.ViewGroup
import com.tomclaw.appsend.util.bdui.factory.BduiComponentFactory
import com.tomclaw.appsend.util.bdui.factory.BduiContainerFactory
import com.tomclaw.appsend.util.bdui.factory.BduiNodeRenderer
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.component.BduiComponent
import com.tomclaw.appsend.util.bdui.model.component.BduiHiddenComponent
import com.tomclaw.appsend.util.bdui.model.container.BduiContainer

/**
 * Main renderer for BDUI nodes.
 * Orchestrates component and container factories to build the view hierarchy.
 */
class BduiRenderer(
    private val componentFactory: BduiComponentFactory,
    private val containerFactory: BduiContainerFactory,
    private val hiddenStorage: BduiHiddenStorage
) : BduiNodeRenderer {

    /**
     * Renders a BDUI node into a View and adds it to the parent.
     *
     * @param node The BDUI node to render
     * @param parent The parent ViewGroup to add the view to
     * @return The created View, or null for hidden components
     */
    override fun render(node: BduiNode, parent: ViewGroup): View? {
        return when (node) {
            is BduiHiddenComponent -> {
                // Hidden components don't create views, just store the value
                hiddenStorage.setHiddenValue(node.id, node.value)
                null
            }
            is BduiContainer -> {
                val view = containerFactory.create(node, parent)
                parent.addView(view)
                view
            }
            is BduiComponent -> {
                val view = componentFactory.create(node, parent)
                view?.let { parent.addView(it) }
                view
            }
            else -> null // Unknown node type
        }
    }

    /**
     * Clears the rendered view hierarchy and storage.
     */
    fun clear() {
        hiddenStorage.clear()
    }
}

