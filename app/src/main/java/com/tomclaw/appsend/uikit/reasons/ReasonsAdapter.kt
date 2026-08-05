package com.tomclaw.appsend.uikit.reasons

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.dto.RejectionReason

// Rows of the reason picker bottom sheet, shared by every screen that
// asks a moderator "why?" — a moderation decline, a takedown, a block.
// Each row is a tap target; what happens next (submit outright, or ask
// for a comment first) is the caller's decision, driven by
// reason.requiresComment.
class ReasonsAdapter(
    private val reasons: List<RejectionReason>,
    private val onClick: (RejectionReason) -> Unit,
) : RecyclerView.Adapter<ReasonsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reason, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = reasons.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reason = reasons[position]
        holder.bind(reason)
        holder.itemView.setOnClickListener { onClick(reason) }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.findViewById(R.id.reason_text)
        private val requiresComment: TextView =
            itemView.findViewById(R.id.reason_requires_comment)

        fun bind(reason: RejectionReason) {
            text.text = reason.text
            requiresComment.visibility =
                if (reason.requiresComment) View.VISIBLE else View.GONE
        }
    }
}
