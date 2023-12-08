package com.tomclaw.appsend.screen.permissions.adapter.unsafe

import android.view.View
import android.widget.TextView
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.bind

interface UnsafePermissionItemView : ItemView {

    fun setDescription(value: String?)

    fun setPermission(value: String)

}

class UnsafePermissionItemViewHolder(view: View) : BaseViewHolder(view), UnsafePermissionItemView {

    private val description: TextView = view.findViewById(R.id.description)
    private val permission: TextView = view.findViewById(R.id.permission)

    override fun setDescription(value: String?) {
        description.bind(value)
    }

    override fun setPermission(value: String) {
        permission.bind(value)
    }

}
