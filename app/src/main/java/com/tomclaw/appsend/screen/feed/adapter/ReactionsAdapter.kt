package com.tomclaw.appsend.screen.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.imageloader.util.centerCrop
import com.tomclaw.imageloader.util.fetch

class ReactionsAdapter : RecyclerView.Adapter<ReactionsAdapter.ViewHolder>() {

    val dataSet = ArrayList<Reaction>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val icon: ImageView = view.findViewById(R.id.reaction_icon)
        private val count: TextView = view.findViewById(R.id.reaction_count)

        fun setReaction(item: Reaction) {
            icon.fetch(item.url) {
                centerCrop()
                placeholder = {
                    with(it.get()) {
                        setImageDrawable(null)
                    }
                }
            }
            count.text = if ((item.count ?: 0) > 0) item.count.toString() else ""
            itemView.alpha = if (item.active == true) 1.0f else 0.7f
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.feed_reaction_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        viewHolder.setReaction(item)
    }

    override fun getItemCount() = dataSet.size

}
