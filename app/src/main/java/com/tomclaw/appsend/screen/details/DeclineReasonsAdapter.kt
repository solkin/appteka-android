package com.tomclaw.appsend.screen.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.api.RejectionReason

// Simple recycler adapter for the decline-reason bottom sheet. Each
// row is a tap target: tapping a reason without a comment requirement
// fires submission immediately; reasons that require a comment hand
// off to a separate dialog driven by the click listener.
class DeclineReasonsAdapter(
    private val reasons: List<RejectionReason>,
    private val onClick: (RejectionReason) -> Unit,
) : RecyclerView.Adapter<DeclineReasonsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_decline_reason, parent, false)
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
