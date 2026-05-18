package com.tomclaw.appsend.screen.chat.adapter.loadmore

import android.view.View
import com.tomclaw.appsend.util.adapter.BaseItemViewHolder
import com.tomclaw.appsend.util.adapter.ItemView

interface LoadMoreItemView : ItemView

class LoadMoreItemViewHolder(view: View) : BaseItemViewHolder(view), LoadMoreItemView {

    override fun onUnbind() = Unit

}
