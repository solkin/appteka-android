package com.tomclaw.appsend.screen.details.adapter.content_flags

import android.view.LayoutInflater
import android.view.View
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.tomclaw.appsend.R
import com.tomclaw.appsend.core.content.ContentFlag
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView

interface ContentFlagsItemView : ItemView {

    fun showFlags(codes: List<String>)

}

class ContentFlagsItemViewHolder(view: View) : BaseItemViewHolder(view), ContentFlagsItemView {

    private val chips: ChipGroup = view.findViewById(R.id.content_flags_chips)

    override fun showFlags(codes: List<String>) {
        chips.removeAllViews()
        val inflater = LayoutInflater.from(itemView.context)
        // Unknown codes are skipped rather than printed raw: the
        // vocabulary is closed, and a code this build has no wording for
        // says nothing to a reader.
        val flags = ContentFlag.entries.filter { it.code in codes }
        for (flag in flags) {
            val chip = inflater.inflate(R.layout.details_content_flag_chip, chips, false) as Chip
            chip.setText(flag.titleRes)
            // Same reasoning as the tag chips: the invisible 48dp touch
            // target would dwarf the group's vertical spacing.
            chip.setEnsureMinTouchTargetSize(false)
            chips.addView(chip)
        }
    }

}
