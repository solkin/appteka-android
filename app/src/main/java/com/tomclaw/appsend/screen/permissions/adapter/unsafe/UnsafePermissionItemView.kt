package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import android.view.View
import android.widget.TextView
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface UnsafePermissionItemView : ItemView {

    fun setDescription(value: String?)

    fun setPermission(value: String)

}

class UnsafePermissionItemViewHolder(view: View) : BaseItemViewHolder(view), UnsafePermissionItemView {

    private val description: TextView = view.findViewById(R.id.description)
    private val permission: TextView = view.findViewById(R.id.permission)

    override fun setDescription(value: String?) {
        description.bind(value)
    }

    override fun setPermission(value: String) {
        permission.bind(value)
    }

}
