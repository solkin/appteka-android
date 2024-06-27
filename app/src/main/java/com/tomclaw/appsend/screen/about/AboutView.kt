package com.tomclaw.appsend.screen.about

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.jakewharton.rxrelay3.PublishRelay
import com.tomclaw.appsend.R
import com.tomclaw.appsend.util.clicks
import io.reactivex.rxjava3.core.Observable

interface AboutView {

    fun setVersion(value: String)

    fun navigationClicks(): Observable<Unit>

    fun feedbackEmailClicks(): Observable<Unit>

    fun forumDiscussClicks(): Observable<Unit>

    fun telegramGroupClicks(): Observable<Unit>

    fun legalInfoClicks(): Observable<Unit>

}

class AboutViewImpl(view: View) : AboutView {

    private val toolbar: Toolbar = view.findViewById(R.id.toolbar)
    private val appVersionText: TextView = view.findViewById(R.id.app_version)
    private val feedbackEmailButton: View = view.findViewById(R.id.feedback_email)
    private val forumDiscussButton: View = view.findViewById(R.id.forum_discuss)
    private val telegramGroupButton: View = view.findViewById(R.id.telegram_group)
    private val legalInfoButton: View = view.findViewById(R.id.legal_info)

    private val navigationRelay = PublishRelay.create<Unit>()
    private val feedbackEmailRelay = PublishRelay.create<Unit>()
    private val forumDiscussRelay = PublishRelay.create<Unit>()
    private val telegramGroupRelay = PublishRelay.create<Unit>()
    private val legalInfoRelay = PublishRelay.create<Unit>()

    init {
        toolbar.setTitle(R.string.info)
        toolbar.setNavigationOnClickListener { navigationRelay.accept(Unit) }

        feedbackEmailButton.clicks(feedbackEmailRelay)
        forumDiscussButton.clicks(forumDiscussRelay)
        telegramGroupButton.clicks(telegramGroupRelay)
        legalInfoButton.clicks(legalInfoRelay)
    }

    override fun setVersion(value: String) {
        appVersionText.text = value
    }

    override fun navigationClicks(): Observable<Unit> = navigationRelay

    override fun feedbackEmailClicks(): Observable<Unit> = feedbackEmailRelay

    override fun forumDiscussClicks(): Observable<Unit> = forumDiscussRelay

    override fun telegramGroupClicks(): Observable<Unit> = telegramGroupRelay

    override fun legalInfoClicks(): Observable<Unit> = legalInfoRelay

}
