package com.tomclaw.appsend.screen.details.adapter.permissions

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface PermissionsItemView : ItemView {

    fun showAccess(
        network: Boolean,
        calls: Boolean,
        sms: Boolean,
        storage: Boolean,
        location: Boolean,
        otherText: String?
    )

    fun setOnClickListener(listener: (() -> Unit)?)

}

class PermissionsItemViewHolder(view: View) : BaseViewHolder(view), PermissionsItemView {

    private val accessNetwork: View = view.findViewById(R.id.access_network)
    private val accessCalls: View = view.findViewById(R.id.access_calls)
    private val accessSms: View = view.findViewById(R.id.access_sms)
    private val accessStorage: View = view.findViewById(R.id.access_storage)
    private val accessLocation: View = view.findViewById(R.id.access_location)
    private val accessOther: TextView = view.findViewById(R.id.access_other)

    private var clickListener: (() -> Unit)? = null

    init {
        view.setOnClickListener { clickListener?.invoke() }
    }

    override fun showAccess(
        network: Boolean,
        calls: Boolean,
        sms: Boolean,
        storage: Boolean,
        location: Boolean,
        otherText: String?
    ) {
        accessNetwork.isVisible = network
        accessCalls.isVisible = calls
        accessSms.isVisible = sms
        accessStorage.isVisible = storage
        accessLocation.isVisible = location
        accessOther.bind(otherText)
    }

    override fun setOnClickListener(listener: (() -> Unit)?) {
        this.clickListener = listener
    }

    override fun onUnbind() {
        this.clickListener = null
    }

}
