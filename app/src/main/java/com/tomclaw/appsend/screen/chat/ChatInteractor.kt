package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.util.SchedulersFactory

interface ChatInteractor {
}

class ChatInteractorImpl(
    private val schedulers: SchedulersFactory
) : ChatInteractor {
}
