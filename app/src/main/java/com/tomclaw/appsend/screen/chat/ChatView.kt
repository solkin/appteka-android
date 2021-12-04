package com.tomclaw.appsend.screen.chat

import android.view.View
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import io.reactivex.rxjava3.core.Observable

interface ChatView {

    fun showProgress()

    fun showContent()

    fun contentUpdated()

    fun contentUpdated(position: Int)

    fun showError()

    fun navigationClicks(): Observable<Unit>

}

class ChatViewImpl(
    view: View,
    private val adapter: SimpleRecyclerAdapter
) : ChatView {

    override fun showProgress() {
    }

    override fun showContent() {
    }

    override fun contentUpdated() {
    }

    override fun contentUpdated(position: Int) {
    }

    override fun showError() {
    }

    override fun navigationClicks(): Observable<Unit> {
        TODO("Not yet implemented")
    }
}