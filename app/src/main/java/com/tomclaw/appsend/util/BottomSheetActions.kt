package com.tomclaw.appsend.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R

/**
 * ActionItem supports both a drawable resource id (iconRes) and an SVG string (iconSvg).
 * If iconRes != 0 -> adapter will try to use the resource first.
 * If iconRes == 0 and iconSvg != null -> adapter will attempt svgToDrawable.
 * If both fail -> adapter will use fallback R.drawable.ic_category.
 */
data class ActionItem(
    val id: Int,
    val title: String,
    val iconRes: Int = 0,     // 0 means "no resource provided"
    val iconSvg: String? = null
)

class ActionsAdapter(
    private val actions: List<ActionItem>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<ActionsAdapter.ActionViewHolder>() {

    class ActionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.action_title)
        val icon: ImageView = view.findViewById(R.id.action_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bottom_sheet_action, parent, false)
        return ActionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActionViewHolder, position: Int) {
        val item = actions[position]
        holder.title.text = item.title

        // 1) If resource icon provided and non-zero -> use it.
        // 2) Else if SVG string provided -> try to convert via svgToDrawable.
        // 3) Else use fallback resource (ic_category).
        val context = holder.itemView.context

        var drawableSet = false

        if (item.iconRes != 0) {
            try {
                holder.icon.setImageResource(item.iconRes)
                drawableSet = true
            } catch (t: Throwable) {
                drawableSet = false
            }
        }

        if (!drawableSet && !item.iconSvg.isNullOrEmpty()) {
            try {
                val drawable = svgToDrawable(item.iconSvg, context.resources)
                if (drawable != null) {
                    holder.icon.setImageDrawable(drawable)
                    drawableSet = true
                }
            } catch (t: Throwable) {
                // conversion failed, will fallback below
                drawableSet = false
            }
        }

        if (!drawableSet) {
            holder.icon.setImageResource(R.drawable.ic_category)
        }

        holder.itemView.setOnClickListener {
            onClick(item.id)
        }
    }

    override fun getItemCount() = actions.size
}

