package com.tomclaw.appsend.screen.profile.adapter.uploads

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface UploadsItemView : ItemView {

    fun setUploadsCount(count: String)

}

class UploadsItemViewHolder(view: View) : BaseViewHolder(view), UploadsItemView {

    private val context = view.context
    private val uploadsCountText: TextView = view.findViewById(R.id.uploads_count)

    override fun setUploadsCount(count: String) {
        uploadsCountText.bind(count)
    }

    override fun onUnbind() {
    }

}
