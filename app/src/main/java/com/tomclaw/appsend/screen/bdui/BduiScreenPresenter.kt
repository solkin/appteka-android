package com.tomclaw.appsend.screen.bdui

import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.bdui.model.BduiNode
import com.tomclaw.appsend.util.bdui.model.action.BduiRpcResponse
import com.tomclaw.appsend.util.bdui.model.action.BduiSequenceAction
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface BduiScreenPresenter {

    fun attachView(view: BduiScreenView)

    fun detachView()

    fun attachRouter(router: BduiScreenRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    interface BduiScreenRouter {

        fun leaveScreen()

        fun handleCallback(name: String, data: Any?)

    }

}

class BduiScreenPresenterImpl(
    private val url: String,
    private val title: String?,
    private val interactor: BduiScreenInteractor,
    private val schedulers: SchedulersFactory,
    state: Bundle?
) : BduiScreenPresenter {

    private var view: BduiScreenView? = null
    private var router: BduiScreenPresenter.BduiScreenRouter? = null

    private var schema: BduiNode? = state?.getSerializable(KEY_SCHEMA) as? BduiNode
    private var isLoading: Boolean = state?.getBoolean(KEY_LOADING, false) ?: false
    private var isError: Boolean = state?.getBoolean(KEY_ERROR, false) ?: false

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: BduiScreenView) {
        this.view = view

        view.setTitle(title)

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.retryClicks().subscribe { loadSchema() }

        subscriptions += view.callbackEvents().subscribe { event ->
            router?.handleCallback(event.name, event.data)
        }

        subscriptions += view.rpcRequests()
            .flatMapSingle { request ->
                interactor.executeRpc(request.action)
                    .observeOn(schedulers.mainThread())
                    .doOnSuccess { response ->
                        request.responseEmitter(response)
                    }
                    .doOnError { error ->
                        request.errorEmitter(error)
                    }
                    .onErrorReturnItem(BduiRpcResponse(BduiSequenceAction(actions = emptyList())))
            }
            .subscribe()

        when {
            schema != null -> view.showContent(schema!!)
            isError -> view.showError()
            else -> loadSchema()
        }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: BduiScreenPresenter.BduiScreenRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putBoolean(KEY_LOADING, isLoading)
        putBoolean(KEY_ERROR, isError)
        // Note: BduiNode is not Serializable by default
        // For full state restoration, you might need to save the JSON string
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    private fun loadSchema() {
        isLoading = true
        isError = false
        view?.showLoading()

        subscriptions += interactor.loadSchema(url)
            .observeOn(schedulers.mainThread())
            .subscribe(
                { loadedSchema ->
                    isLoading = false
                    schema = loadedSchema
                    view?.showContent(loadedSchema)
                },
                { error ->
                    isLoading = false
                    isError = true
                    view?.showError()
                }
            )
    }

}

private const val KEY_SCHEMA = "schema"
private const val KEY_LOADING = "loading"
private const val KEY_ERROR = "error"

