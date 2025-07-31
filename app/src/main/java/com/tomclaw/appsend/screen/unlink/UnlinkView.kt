package com.tomclaw.appsend.screen.unlink

import android.view.View

interface UnlinkView {

    fun showProgress()

    fun showContent()

}

class UnlinkViewImpl(
        private val view: View
) : UnlinkView {

    init {

    }

    override fun showProgress() {

    }

    override fun showContent() {

    }

}
