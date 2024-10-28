package com.tomclaw.appsend.screen.about

import android.os.Bundle
import com.tomclaw.appsend.core.UserAgentProvider
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface AboutPresenter {

    fun attachView(view: AboutView)

    fun detachView()

    fun attachRouter(router: AboutRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface AboutRouter {

        fun openFeedbackEmail(addr: String, subject: String, text: String)

        fun openForumDiscussLink()

        fun openTelegramGroupLink()

        fun openLegalInfoLink()

        fun leaveScreen()
    }

}

class AboutPresenterImpl(
    private val resourceProvider: AboutResourceProvider,
    private val userAgentProvider: UserAgentProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : AboutPresenter {

    private var view: AboutView? = null
    private var router: AboutPresenter.AboutRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: AboutView) {
        this.view = view

        bindData()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.feedbackEmailClicks().subscribe {
            router?.openFeedbackEmail(
                addr = "support@appteka.store",
                subject = userAgentProvider.getUserAgent(),
                text = ""
            )
        }
        subscriptions += view.forumDiscussClicks().subscribe { router?.openForumDiscussLink() }
        subscriptions += view.telegramGroupClicks().subscribe { router?.openTelegramGroupLink() }
        subscriptions += view.legalInfoClicks().subscribe { router?.openLegalInfoLink() }
    }

    private fun bindData() {
        view?.setVersion(resourceProvider.getAppVersion())
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: AboutPresenter.AboutRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {}

    override fun onBackPressed() {
        router?.leaveScreen()
    }

}
