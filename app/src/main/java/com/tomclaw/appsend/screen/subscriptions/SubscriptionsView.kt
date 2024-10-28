package com.tomclaw.appsend.screen.subscriptions

import android.view.View
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import io.reactivex.rxjava3.core.Observable

interface SubscriptionsView {
    fun navigationClicks(): Observable<Unit>
}

class SubscriptionsViewImpl(
    view: View,
) : SubscriptionsView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)

    private val navigationRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.subscribers)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

}