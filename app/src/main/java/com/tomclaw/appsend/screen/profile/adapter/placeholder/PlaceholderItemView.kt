package com.tomclaw.appsend.screen.profile.adapter.placeholder

import android.view.View
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView

interface PlaceholderItemView : ItemView

class PlaceholderItemViewHolder(view: View) : BaseItemViewHolder(view), PlaceholderItemView {

    override fun onUnbind() {}

}
