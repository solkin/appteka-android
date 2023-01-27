package com.tomclaw.appsend.screen.upload

import com.tomclaw.appsend.util.SchedulersFactory

interface UploadInteractor {

}

class UploadInteractorImpl(
        private val schedulers: SchedulersFactory
) : UploadInteractor {

}