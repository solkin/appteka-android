package com.tomclaw.appsend.screen.feed.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.appsend.util.bind
import com.tomclaw.imageloader.util.fetch

class ReactionsAdapter(
    private var clickListener: ((Reaction) -> Unit)? = null
) : RecyclerView.Adapter<ReactionsAdapter.ViewHolder>() {

    val dataSet = ArrayList<Reaction>()

    fun setClickListener(listener: ((Reaction) -> Unit)?) {
        this.clickListener = listener
    }

    class ViewHolder(
        private val view: View,
        private val adapter: ReactionsAdapter
    ) : RecyclerView.ViewHolder(view) {

        private val container: View = view.findViewById(R.id.reaction_container)
        private val icon: ImageView = view.findViewById(R.id.reaction_icon)
        private val count: TextView = view.findViewById(R.id.reaction_count)

        private var clickListener: ((Reaction) -> Unit)? = null

        fun setReaction(item: Reaction, listener: ((Reaction) -> Unit)?) {
            this.clickListener = listener
            view.setOnClickListener {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < adapter.dataSet.size) {
                    clickListener?.invoke(adapter.dataSet[adapterPosition])
                }
            }
            icon.fetch(item.url) {
                centerCrop()
                onLoading { imageView ->
                    imageView.setImageDrawable(null)
                }
            }
            count.bind(value = if ((item.count ?: 0) > 0) item.count.toString() else "")
            
            // Применяем стиль в зависимости от активности реакции (как в Slack)
            if (item.active == true) {
                container.setBackgroundResource(R.drawable.reaction_background_active)
            } else {
                container.setBackgroundResource(R.drawable.reaction_background)
            }
        }

        fun onUnbind() {
            this.clickListener = null
            view.setOnClickListener(null)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.feed_reaction_item, viewGroup, false)
        return ViewHolder(view, this)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = dataSet[position]
        viewHolder.setReaction(item, clickListener)
    }

    override fun getItemCount() = dataSet.size

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.onUnbind()
    }

}
