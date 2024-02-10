package com.tomclaw.appsend.screen.profile

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
        TODO("Not yet implemented")
    }

    override fun showContent() {
        TODO("Not yet implemented")
    }

    override fun showError() {
        TODO("Not yet implemented")
    }

    override fun contentUpdated() {
        TODO("Not yet implemented")
    }

    override fun contentUpdated(position: Int) {
        TODO("Not yet implemented")
    }

}