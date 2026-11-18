package com.tomclaw.appsend.screen.details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.tomclaw.appsend.R

/** The versions sheet: editions as headers, each followed by its versions. */
class VersionsAdapter(
    private val entries: List<VersionsEntry>,
    private val onVersionClick: (VersionItem) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private class EditionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.edition_title)
        val installed: View = view.findViewById(R.id.edition_installed)
        val summary: TextView = view.findViewById(R.id.edition_summary)
    }

    private class VersionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.action_title)
        val icon: ImageView = view.findViewById(R.id.action_icon)
    }

    override fun getItemViewType(position: Int): Int = when (entries[position]) {
        is EditionHeader -> TYPE_EDITION
        is VersionItem -> TYPE_VERSION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_EDITION -> EditionViewHolder(
                inflater.inflate(R.layout.item_versions_edition, parent, false).also {
                    ViewCompat.setAccessibilityHeading(it, true)
                }
            )

            else -> VersionViewHolder(
                inflater.inflate(R.layout.item_bottom_sheet_action, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val entry = entries[position]) {
            is EditionHeader -> with(holder as EditionViewHolder) {
                val top = if (position == 0) {
                    R.dimen.versions_first_edition_top
                } else {
                    R.dimen.versions_edition_top
                }
                itemView.updatePadding(top = itemView.resources.getDimensionPixelSize(top))
                title.text = entry.title
                installed.isVisible = entry.installed
                summary.text = entry.summary
            }

            is VersionItem -> with(holder as VersionViewHolder) {
                title.text = entry.title
                icon.setImageResource(
                    when {
                        !entry.compatible -> R.drawable.ic_alert_circle
                        entry.newer -> R.drawable.ic_new
                        else -> R.drawable.ic_download_circle
                    }
                )
                itemView.setOnClickListener { onVersionClick(entry) }
            }
        }
    }

    override fun getItemCount(): Int = entries.size

}

private const val TYPE_EDITION = 0
private const val TYPE_VERSION = 1
