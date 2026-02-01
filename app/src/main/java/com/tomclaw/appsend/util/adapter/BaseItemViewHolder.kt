package com.tomclaw.appsend.util.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseItemViewHolder(
    view: View
) : RecyclerView.ViewHolder(view), ItemView {

    open fun onUnbind() {}

}
