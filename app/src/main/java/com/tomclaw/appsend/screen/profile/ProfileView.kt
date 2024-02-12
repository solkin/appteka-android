package com.tomclaw.appsend.screen.profile

import android.annotation.SuppressLint
import android.view.View
import com.avito.konveyor.adapter.SimpleRecyclerAdapter

interface ProfileView {

    fun showProgress()

    fun showContent()

    fun showError()

    fun contentUpdated()

    fun contentUpdated(position: Int)

}

class ProfileViewImpl(
    private val view: View,
    private val adapter: SimpleRecyclerAdapter
) : ProfileView {

    override fun showProgress() {
    }

    override fun showContent() {
    }

    override fun showError() {
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun contentUpdated() {
        adapter.notifyDataSetChanged()
    }

    override fun contentUpdated(position: Int) {
        adapter.notifyItemChanged(position)
    }

}