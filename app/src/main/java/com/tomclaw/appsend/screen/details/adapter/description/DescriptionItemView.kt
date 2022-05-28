package com.tomclaw.appsend.screen.details.adapter.description

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface DescriptionItemView : ItemView {

    fun setText(value: String)

    fun setAppVersion(value: String)

    fun setUploadDate(value: String)

    fun setChecksum(value: String)

}

class DescriptionItemViewHolder(view: View) : BaseViewHolder(view), DescriptionItemView {

    private val description: TextView = view.findViewById(R.id.description)
    private val appVersion: TextView = view.findViewById(R.id.app_version)
    private val uploadDate: TextView = view.findViewById(R.id.upload_date)
    private val checksum: TextView = view.findViewById(R.id.app_checksum)

    override fun setText(value: String) {
        description.bind(value)
    }

    override fun setAppVersion(value: String) {
        appVersion.bind(value)
    }

    override fun setUploadDate(value: String) {
        uploadDate.bind(value)
    }

    override fun setChecksum(value: String) {
        checksum.bind(value)
    }

}
