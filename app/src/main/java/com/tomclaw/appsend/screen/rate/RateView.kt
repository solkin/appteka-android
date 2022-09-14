package com.tomclaw.appsend.screen.rate

import android.view.View

interface RateView {

    fun setTitle(title: String)

    fun setIcon(icon: String?)

}

class RateViewImpl(view: View) : RateView {

    override fun setTitle(title: String) {
    }

    override fun setIcon(icon: String?) {
    }

}
