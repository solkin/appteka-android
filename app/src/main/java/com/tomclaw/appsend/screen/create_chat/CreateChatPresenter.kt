package com.tomclaw.appsend.screen.create_chat

import android.net.Uri
import android.os.Bundle
import com.tomclaw.appsend.util.SchedulersFactory
import com.tomclaw.appsend.util.filterCapabilityErrors
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign

interface CreateChatPresenter {

    fun attachView(view: CreateChatView)

    fun detachView()

    fun attachRouter(router: CreateChatRouter)

    fun detachRouter()

    fun saveState(): Bundle

    fun onBackPressed()

    fun onAvatarPicked(uri: Uri)

    fun onLoginSucceeded()

    interface CreateChatRouter {

        fun leaveScreen()

        fun openChatScreen(topicId: Int, title: String)

        fun openLoginScreen()

    }

}

class CreateChatPresenterImpl(
    private val interactor: CreateChatInteractor,
    private val resourceProvider: CreateChatResourceProvider,
    private val schedulers: SchedulersFactory,
    state: Bundle?,
) : CreateChatPresenter {

    private var view: CreateChatView? = null
    private var router: CreateChatPresenter.CreateChatRouter? = null

    private var title: String = state?.getString(KEY_TITLE).orEmpty()
    private var description: String = state?.getString(KEY_DESCRIPTION).orEmpty()
    private var avatarUri: Uri? = state?.getString(KEY_AVATAR_URI)?.let(Uri::parse)

    private val subscriptions = CompositeDisposable()

    override fun attachView(view: CreateChatView) {
        this.view = view

        view.setTitle(title)
        view.setDescription(description)
        avatarUri?.let { view.setAvatar(it) }
        bindSubmitState()

        subscriptions += view.navigationClicks().subscribe { onBackPressed() }
        subscriptions += view.titleChanged().subscribe {
            title = it
            bindSubmitState()
        }
        subscriptions += view.descriptionChanged().subscribe {
            description = it
            bindSubmitState()
        }
        subscriptions += view.avatarClicks().subscribe { view.openAvatarPicker() }
        subscriptions += view.submitClicks().subscribe { onSubmit() }
    }

    override fun detachView() {
        subscriptions.clear()
        this.view = null
    }

    override fun attachRouter(router: CreateChatPresenter.CreateChatRouter) {
        this.router = router
    }

    override fun detachRouter() {
        this.router = null
    }

    override fun saveState(): Bundle = Bundle().apply {
        putString(KEY_TITLE, title)
        putString(KEY_DESCRIPTION, description)
        putString(KEY_AVATAR_URI, avatarUri?.toString())
    }

    override fun onBackPressed() {
        router?.leaveScreen()
    }

    override fun onAvatarPicked(uri: Uri) {
        avatarUri = uri
        view?.setAvatar(uri)
        bindSubmitState()
    }

    override fun onLoginSucceeded() {
        if (avatarUri != null && isTitleValid() && isDescriptionValid()) {
            onSubmit()
        }
    }

    private fun bindSubmitState() {
        val canSubmit = avatarUri != null && isTitleValid() && isDescriptionValid()
        if (canSubmit) view?.enableSubmitButton() else view?.disableSubmitButton()
    }

    private fun isTitleValid(): Boolean =
        title.trim().length in TITLE_MIN_LENGTH..TITLE_MAX_LENGTH

    private fun isDescriptionValid(): Boolean =
        description.trim().length in DESCRIPTION_MIN_LENGTH..DESCRIPTION_MAX_LENGTH

    private fun onSubmit() {
        val avatar = avatarUri
        if (avatar == null) {
            view?.showValidationError(resourceProvider.avatarRequiredError())
            return
        }
        if (!isTitleValid()) {
            view?.showValidationError(resourceProvider.titleTooShortError(TITLE_MIN_LENGTH))
            return
        }
        if (!isDescriptionValid()) {
            view?.showValidationError(resourceProvider.descriptionTooShortError(DESCRIPTION_MIN_LENGTH))
            return
        }
        subscriptions += interactor.createTopic(title.trim(), description.trim(), avatar)
            .observeOn(schedulers.mainThread())
            .doOnSubscribe { view?.showProgress() }
            .subscribe(
                { topic -> router?.openChatScreen(topic.topicId, topic.title) },
                { ex ->
                    view?.showContent()
                    ex.filterCapabilityErrors(
                        authError = { router?.openLoginScreen() },
                        capabilityDenied = { cap -> view?.showCapabilityDenied(cap) },
                        other = { view?.showError(resourceProvider.createTopicError()) },
                    )
                },
            )
    }

}

const val TITLE_MIN_LENGTH = 3
const val TITLE_MAX_LENGTH = 100
const val DESCRIPTION_MIN_LENGTH = 3
const val DESCRIPTION_MAX_LENGTH = 500

private const val KEY_TITLE = "title"
private const val KEY_DESCRIPTION = "description"
private const val KEY_AVATAR_URI = "avatar_uri"
