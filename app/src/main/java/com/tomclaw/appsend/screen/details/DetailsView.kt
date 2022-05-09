package com.tomclaw.appsend.screen.details

import android.view.View

interface DetailsView {

    fun showProgress()

    fun showContent()

}

class DetailsViewImpl(
        private val view: View
) : DetailsView {

    init {

    }

    override fun showProgress() {

    }

    override fun showContent() {

    }

}
