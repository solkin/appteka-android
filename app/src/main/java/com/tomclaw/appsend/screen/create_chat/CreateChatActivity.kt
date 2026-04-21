package com.tomclaw.appsend.screen.create_chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.chat.createChatActivityIntent
import com.tomclaw.appsend.screen.create_chat.di.CreateChatModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class CreateChatActivity : AppCompatActivity(), CreateChatPresenter.CreateChatRouter {

    @Inject
    lateinit var presenter: CreateChatPresenter

    @Inject
    lateinit var analytics: Analytics

    private val pickAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) presenter.onAvatarPicked(uri)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .createChatComponent(CreateChatModule(this, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_chat_activity)

        val view = CreateChatViewImpl(window.decorView, avatarPickerLauncher = ::launchAvatarPicker)
        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-create-chat-screen")
        }
    }

    override fun onStart() {
        super.onStart()
        presenter.attachRouter(this)
    }

    override fun onStop() {
        presenter.detachRouter()
        super.onStop()
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(KEY_PRESENTER_STATE, presenter.saveState())
    }

    override fun leaveScreen() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun openChatScreen(topicId: Int, title: String) {
        analytics.trackEvent("create-chat-success")
        setResult(Activity.RESULT_OK)
        startActivity(createChatActivityIntent(this, topicId = topicId, title = title))
        finish()
    }

    override fun openLoginScreen() {
        startActivity(createRequestCodeActivityIntent(this))
    }

    private fun launchAvatarPicker() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        pickAvatarLauncher.launch(request)
    }

}

fun createCreateChatActivityIntent(context: Context): Intent =
    Intent(context, CreateChatActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
