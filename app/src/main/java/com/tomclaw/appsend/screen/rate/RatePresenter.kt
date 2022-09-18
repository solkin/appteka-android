package com.tomclaw.appsend.screen.rate

import android.os.Bundle
import android.text.TextUtils
import com.tomclaw.appsend.categories.DEFAULT_LOCALE
import com.tomclaw.appsend.dto.UserData
import com.tomclaw.appsend.user.UserDataInteractor
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.Locale

interface RatePresenter {

    fun attachView(view: RateView)

    fun detachView()

    fun attachRouter(router: RateRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface RateRouter {

        fun leaveScreen()

    }

}

class RatePresenterImpl(
    private val appId: String,
    private val interactor: RateInteractor,
    private val userDataInteractor: UserDataInteractor,
    private val locale: Locale,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : RatePresenter {

    private var view: RateView? = null
    private var router: RatePresenter.RateRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: RateView) {
        this.view = view

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += userDataInteractor
            .getUserData()
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.mainThread())
            .subscribe({ userData ->
                bindMemberInfo(userData)
            }, {})
    }

    private fun bindMemberInfo(userData: UserData) {
        view?.setMemberIcon(userData.userIcon)
        val name = userData.name.takeIf { !it.isNullOrBlank() }
            ?: userData.userIcon.label[locale.language]
            ?: userData.userIcon.label[DEFAULT_LOCALE].orEmpty()
        view?.setMemberName(name)
    }

    override fun detachView() {
        this.view = null
    }

    override fun attachRouter(router: RatePresenter.RateRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

}
