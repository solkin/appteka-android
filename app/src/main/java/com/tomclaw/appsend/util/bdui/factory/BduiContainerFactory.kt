package com.tomclaw.appsend.util.bdui.factory

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.ViewFlipper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.util.bdui.BduiActionHandler
import com.tomclaw.appsend.util.bdui.BduiViewRegistry
import com.tomclaw.appsend.util.bdui.model.BduiLayoutParams
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.container.BduiContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiFlipperContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiFrameContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiLinearContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiRecyclerContainer
import com.tomclaw.appsend.util.bdui.model.container.BduiScrollContainer

/**
 * Factory for creating Android ViewGroups from BDUI container models.
 */
class BduiContainerFactory(
    private val context: Context,
    private val layoutParamsFactory: BduiLayoutParamsFactory,
    private val viewRegistry: BduiViewRegistry,
    private val actionHandler: BduiActionHandler,
    private val nodeRenderer: () -> BduiNodeRenderer
) {

    fun create(container: BduiContainer, parent: ViewGroup): ViewGroup {
        val view = when (container) {
            is BduiFrameContainer -> createFrame(container)
            is BduiLinearContainer -> createLinear(container)
            is BduiRecyclerContainer -> createRecycler(container)
            is BduiScrollContainer -> createScroll(container)
            is BduiFlipperContainer -> createFlipper(container)
        }

        applyLayoutParams(view, container.layoutParams, parent)
        setupAction(view, container)
        viewRegistry.registerView(container.id, view)

        // Render children
        renderChildren(container, view)

        return view
    }

    private fun createFrame(container: BduiFrameContainer): ViewGroup {
        return FrameLayout(context)
    }

    private fun createLinear(container: BduiLinearContainer): ViewGroup {
        return LinearLayout(context).apply {
            orientation = if ((container.orientation ?: "vertical") == "horizontal") {
                LinearLayout.HORIZONTAL
            } else {
                LinearLayout.VERTICAL
            }
            container.weightSum?.let { weightSum = it }
            container.gravity?.let { gravity ->
                setGravity(parseGravity(gravity))
            }
        }
    }

    private fun createRecycler(container: BduiRecyclerContainer): ViewGroup {
        val spanCount = container.spanCount ?: 1
        val reverseLayout = container.reverseLayout ?: false
        val isHorizontal = (container.orientation ?: "vertical") == "horizontal"
        return RecyclerView(context).apply {
            layoutManager = if (spanCount > 1) {
                GridLayoutManager(
                    context,
                    spanCount,
                    if (isHorizontal) GridLayoutManager.HORIZONTAL else GridLayoutManager.VERTICAL,
                    reverseLayout
                )
            } else {
                LinearLayoutManager(
                    context,
                    if (isHorizontal) LinearLayoutManager.HORIZONTAL else LinearLayoutManager.VERTICAL,
                    reverseLayout
                )
            }

            container.itemSpacing?.let { spacing ->
                addItemDecoration(BduiItemSpacingDecoration(dpToPx(spacing)))
            }

            // Set up adapter with children as items
            container.children?.let { children ->
                adapter = BduiRecyclerAdapter(children, nodeRenderer())
            }
        }
    }

    private fun createScroll(container: BduiScrollContainer): ViewGroup {
        val fillViewport = container.fillViewport ?: false
        return if ((container.orientation ?: "vertical") == "horizontal") {
            HorizontalScrollView(context).apply {
                isFillViewport = fillViewport
            }
        } else {
            ScrollView(context).apply {
                isFillViewport = fillViewport
            }
        }
    }

    private fun createFlipper(container: BduiFlipperContainer): ViewGroup {
        return ViewFlipper(context).apply {
            // Set flip interval
            flipInterval = container.flipInterval ?: 3000

            // Set in animation
            container.inAnimation?.let { animName ->
                inAnimation = getAnimation(animName)
            }

            // Set out animation
            container.outAnimation?.let { animName ->
                outAnimation = getAnimation(animName)
            }
        }
    }

    private fun getAnimation(name: String): android.view.animation.Animation? {
        val animRes = when (name.lowercase()) {
            "fade_in" -> android.R.anim.fade_in
            "fade_out" -> android.R.anim.fade_out
            "slide_in_left" -> android.R.anim.slide_in_left
            "slide_out_right" -> android.R.anim.slide_out_right
            "slide_in_right" -> com.tomclaw.appsend.R.anim.slide_in_right
            "slide_out_left" -> com.tomclaw.appsend.R.anim.slide_out_left
            else -> return null
        }
        return AnimationUtils.loadAnimation(context, animRes)
    }

    private fun renderChildren(container: BduiContainer, view: ViewGroup) {
        // RecyclerView handles its children via adapter
        if (container is BduiRecyclerContainer) return

        // Scroll containers should have a single child (usually a LinearLayout)
        if (container is BduiScrollContainer) {
            val contentLayout = LinearLayout(context).apply {
                orientation = if ((container.orientation ?: "vertical") == "horizontal") {
                    LinearLayout.HORIZONTAL
                } else {
                    LinearLayout.VERTICAL
                }
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            view.addView(contentLayout)

            container.children?.forEach { child ->
                nodeRenderer().render(child, contentLayout)
            }
            return
        }

        // Flipper containers - render children and set displayed child
        if (container is BduiFlipperContainer && view is ViewFlipper) {
            container.children?.forEach { child ->
                nodeRenderer().render(child, view)
            }
            // Set displayed child after all children are added
            if (container.displayedChild >= 0 && container.displayedChild < (container.children?.size ?: 0)) {
                view.displayedChild = container.displayedChild
            }
            // Start auto-flipping if enabled
            if (container.autoStart) {
                view.startFlipping()
            }
            return
        }

        // Regular containers - render children directly
        container.children?.forEach { child ->
            nodeRenderer().render(child, view)
        }
    }

    private fun applyLayoutParams(view: View, params: BduiLayoutParams?, parent: ViewGroup) {
        view.layoutParams = layoutParamsFactory.create(params, parent)
        params?.let {
            it.visibility?.let { vis ->
                view.visibility = when (vis.lowercase()) {
                    "visible" -> View.VISIBLE
                    "invisible" -> View.INVISIBLE
                    "gone" -> View.GONE
                    else -> View.VISIBLE
                }
            }
            it.alpha?.let { alpha -> view.alpha = alpha }
            it.enabled?.let { enabled -> view.isEnabled = enabled }
            it.clickable?.let { clickable -> view.isClickable = clickable }
            it.padding?.let { padding ->
                view.setPadding(
                    dpToPx(padding.getLeft()),
                    dpToPx(padding.getTop()),
                    dpToPx(padding.getRight()),
                    dpToPx(padding.getBottom())
                )
            }
        }
    }

    private fun setupAction(view: View, container: BduiContainer) {
        container.action?.let { action ->
            view.setOnClickListener {
                actionHandler.execute(action)
                    .subscribe({}, { error ->
                        // Log error or handle failure
                    })
            }
        }
    }

    private fun parseGravity(gravity: String): Int {
        var result = 0
        gravity.split("|").forEach { part ->
            result = result or when (part.trim().lowercase()) {
                "center" -> android.view.Gravity.CENTER
                "center_horizontal" -> android.view.Gravity.CENTER_HORIZONTAL
                "center_vertical" -> android.view.Gravity.CENTER_VERTICAL
                "start", "left" -> android.view.Gravity.START
                "end", "right" -> android.view.Gravity.END
                "top" -> android.view.Gravity.TOP
                "bottom" -> android.view.Gravity.BOTTOM
                "fill" -> android.view.Gravity.FILL
                "fill_horizontal" -> android.view.Gravity.FILL_HORIZONTAL
                "fill_vertical" -> android.view.Gravity.FILL_VERTICAL
                else -> 0
            }
        }
        return if (result == 0) android.view.Gravity.START else result
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}

/**
 * RecyclerView adapter for BDUI nodes.
 */
class BduiRecyclerAdapter(
    private val items: List<BduiNode>,
    private val nodeRenderer: BduiNodeRenderer
) : RecyclerView.Adapter<BduiRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val container = FrameLayout(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
        }
        return ViewHolder(container)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.container.removeAllViews()
        nodeRenderer.render(items[position], holder.container)
    }

    override fun getItemCount(): Int = items.size
}

/**
 * Item decoration for spacing between RecyclerView items.
 */
class BduiItemSpacingDecoration(
    private val spacing: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: android.graphics.Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = spacing / 2
        outRect.right = spacing / 2
        outRect.top = spacing / 2
        outRect.bottom = spacing / 2
    }
}

