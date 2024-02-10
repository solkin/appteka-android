package com.tomclaw.appsend.screen.profile

import android.os.Bundle
import com.avito.konveyor.adapter.AdapterPresenter
import com.tomclaw.appsend.screen.profile.adapter.ItemListener
import com.tomclaw.appsend.util.SchedulersFactory
import dagger.Lazy

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

    override fun attachView(view: ProfileView) {
        TODO("Not yet implemented")
    }

    override fun detachView() {
        TODO("Not yet implemented")
    }

    override fun attachRouter(router: ProfilePresenter.ProfileRouter) {
        TODO("Not yet implemented")
    }

    override fun detachRouter() {
        TODO("Not yet implemented")
    }

    override fun saveState(): Bundle {
        TODO("Not yet implemented")
    }

}