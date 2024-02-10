package com.tomclaw.appsend.screen.profile.adapter.header

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView


interface HeaderItemView : ItemView {

}

class HeaderItemViewHolder(view: View) : BaseViewHolder(view), HeaderItemView {

    override fun onUnbind() {
    }

}
