package com.tomclaw.appsend.screen.profile

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface ProfilePresenter : ItemListener {

    fun attachView(view: ProfileView)

    fun detachView()

    fun attachRouter(router: ProfileRouter)

    fun detachRouter()

    fun saveState(): Bundle

    interface ProfileRouter

}

class ProfilePresenterImpl(
    private val interactor: ProfileInteractor,
    private val adapterPresenter: Lazy<AdapterPresenter>,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : ProfilePresenter {

    private var view: ProfileView? = null
    private var router: ProfilePresenter.ProfileRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: ProfileView) {
        this.view = view
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: ProfilePresenter.ProfileRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
    }

}