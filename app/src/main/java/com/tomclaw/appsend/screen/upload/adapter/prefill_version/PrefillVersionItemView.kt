package com.tomclaw.appsend.screen.upload.adapter.prefill_version

import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.google.android.material.textfield.TextInputLayout
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.upload.adapter.other_versions.VersionItem

interface PrefillVersionItemView : ItemView {

    fun setVersions(versions: List<VersionItem>, selectedVersion: VersionItem?)

    fun setOnVersionSelectedListener(listener: ((VersionItem?) -> Unit)?)

}

class PrefillVersionItemViewHolder(view: View) : BaseViewHolder(view), PrefillVersionItemView {

    private val context = view.context
    private val textInputLayout: TextInputLayout = view.findViewById(R.id.prefill_version_layout)
    private val autoCompleteTextView: AutoCompleteTextView =
        view.findViewById(R.id.prefill_version_dropdown)

    private var versions: List<VersionItem> = emptyList()
    private var onVersionSelectedListener: ((VersionItem?) -> Unit)? = null

    override fun setVersions(versions: List<VersionItem>, selectedVersion: VersionItem?) {
        this.versions = versions

        val items = listOf(
            context.getString(R.string.prefill_none)
        ) + versions.map { it.title }

        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_dropdown_item_1line,
            items
        )
        autoCompleteTextView.setAdapter(adapter)

        val selectedText = selectedVersion?.title
            ?: context.getString(R.string.prefill_none)
        autoCompleteTextView.setText(selectedText, false)

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            val selected = if (position == 0) null else versions.getOrNull(position - 1)
            onVersionSelectedListener?.invoke(selected)
        }
    }

    override fun setOnVersionSelectedListener(listener: ((VersionItem?) -> Unit)?) {
        onVersionSelectedListener = listener
    }

    override fun onUnbind() {
        onVersionSelectedListener = null
    }

}
