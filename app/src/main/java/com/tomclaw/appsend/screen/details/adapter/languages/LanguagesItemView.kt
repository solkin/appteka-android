package com.tomclaw.appsend.screen.details.adapter.languages

import android.view.View
import android.widget.TextView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView

interface LanguagesItemView : ItemView {

    fun showLanguages(names: List<String>)

}

class LanguagesItemViewHolder(view: View) : BaseItemViewHolder(view), LanguagesItemView {

    private val languagesText: TextView = view.findViewById(R.id.languages_text)

    init {
        // Some apps ship dozens of languages: the list folds to a few
        // lines and a tap unfolds it in place.
        view.setOnClickListener { languagesText.maxLines = Int.MAX_VALUE }
    }

    override fun showLanguages(names: List<String>) {
        val text = names.joinToString(", ")
        // The screen rebinds on every download progress tick; the same
        // list must stay as the user left it.
        if (languagesText.text.toString() == text) return
        languagesText.text = text
        languagesText.maxLines = COLLAPSED_LINES
    }

    override fun onUnbind() {
        // No-op
    }

}

private const val COLLAPSED_LINES = 3
