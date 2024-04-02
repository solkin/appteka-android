package com.tomclaw.appsend.screen.profile.adapter.placeholder

import android.view.View
import com.avito.konveyor.adapter.BaseViewHolder
import com.avito.konveyor.blueprint.ItemView

interface PlaceholderItemView : ItemView

class PlaceholderItemViewHolder(view: View) : BaseViewHolder(view), PlaceholderItemView {

    override fun onUnbind() {}

}
