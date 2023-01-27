package com.tomclaw.appsend.screen.upload

import android.view.View

interface UploadView {

    fun showProgress()

    fun showContent()

}

class UploadViewImpl(
        private val view: View
) : UploadView {

    init {

    }

    override fun showProgress() {

    }

    override fun showContent() {

    }

}
