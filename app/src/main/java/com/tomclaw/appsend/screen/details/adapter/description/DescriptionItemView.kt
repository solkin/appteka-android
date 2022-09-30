package com.tomclaw.appsend.screen.details.adapter.description

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface DescriptionItemView : ItemView {

    fun setText(value: String)

    fun setAppVersion(value: String)

    fun setVersionsCount(count: Int)

    fun setUploadDate(value: String)

    fun setChecksum(value: String)

    fun setOnVersionsClickListener(listener: (() -> Unit)?)

}

class DescriptionItemViewHolder(view: View) : BaseViewHolder(view), DescriptionItemView {

    private val context = view.context
    private val description: TextView = view.findViewById(R.id.description)
    private val appVersion: TextView = view.findViewById(R.id.app_version)
    private val versionsButton: View = view.findViewById(R.id.versions_button)
    private val versionsButtonText: TextView = view.findViewById(R.id.versions_button_text)
    private val uploadDate: TextView = view.findViewById(R.id.upload_date)
    private val checksum: TextView = view.findViewById(R.id.app_checksum)

    private var versionsClickListener: (() -> Unit)? = null

    init {
        versionsButton.setOnClickListener { versionsClickListener?.invoke() }
    }

    override fun setText(value: String) {
        description.bind(value)
    }

    override fun setAppVersion(value: String) {
        appVersion.bind(value)
    }

    override fun setVersionsCount(count: Int) {
        versionsButton.isVisible = count > 1
        versionsButtonText.text = context.resources.getQuantityString(
            R.plurals.other_versions_count,
            count,
            count
        )
    }

    override fun setUploadDate(value: String) {
        uploadDate.bind(value)
    }

    override fun setChecksum(value: String) {
        checksum.bind(value)
    }

    override fun setOnVersionsClickListener(listener: (() -> Unit)?) {
        this.versionsClickListener = listener
    }

    override fun onUnbind() {
        this.versionsClickListener = null
    }

}
