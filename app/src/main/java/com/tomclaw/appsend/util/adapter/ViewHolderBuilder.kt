package com.tomclaw.appsend.util.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

object ViewHolderBuilder {

    class ViewHolderProvider(
        val layoutId: Int,
        val creator: (ViewGroup, View) -> BaseItemViewHolder
    ) {
        fun createViewHolder(parent: ViewGroup): BaseItemViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
            return creator(parent, view)
        }
    }

}
