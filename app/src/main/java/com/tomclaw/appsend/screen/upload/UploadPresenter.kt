package com.tomclaw.appsend.screen.upload

import android.content.pm.PackageInfo
import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import io.reactivex.rxjava3.disposables.CompositeDisposable

interface UploadPresenter {

    fun attachView(view: UploadView)

    fun detachView()

    fun attachRouter(router: UploadRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface UploadRouter {

        fun leaveScreen()

    }

}

class UploadPresenterImpl(
    private val info: PackageInfo,
    private val interactor: UploadInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : UploadPresenter {

    private var view: UploadView? = null
    private var router: UploadPresenter.UploadRouter? = null

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: UploadView) {
        this.view = view
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: UploadPresenter.UploadRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState() = Bundle().apply {
    }

    override fun onBackPressed() {
    }

}
