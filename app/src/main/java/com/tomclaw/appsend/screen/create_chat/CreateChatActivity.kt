package com.tomclaw.appsend.screen.create_chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.avatar_crop.createAvatarCropActivityIntent
import com.tomclaw.appsend.screen.avatar_crop.extractAvatarCropResultUri
import com.tomclaw.appsend.screen.chat.createChatActivityIntent
import com.tomclaw.appsend.screen.create_chat.di.CreateChatModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class CreateChatActivity : AppCompatActivity(), CreateChatPresenter.CreateChatRouter {

    @Inject
    lateinit var presenter: CreateChatPresenter

    @Inject
    lateinit var analytics: Analytics

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) presenter.onAvatarPicked(uri)
        }

    private val cropLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val cropped = extractAvatarCropResultUri(result.data)
                if (cropped != null) {
                    presenter.onAvatarCropped(cropped)
                } else {
                    presenter.onAvatarPickFailed()
                }
            }
            // RESULT_CANCELED ⇒ user backed out of the cropper,
            // nothing to do; the editor stays as it was.
        }

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                presenter.onLoginSucceeded()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .createChatComponent(CreateChatModule(this, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_chat_activity)

        val view = CreateChatViewImpl(window.decorView)
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
        loginLauncher.launch(createRequestCodeActivityIntent(this))
    }

    override fun openImagePicker() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        pickImageLauncher.launch(request)
    }

    override fun openCropper(srcUri: Uri) {
        cropLauncher.launch(createAvatarCropActivityIntent(this, srcUri))
    }

}

fun createCreateChatActivityIntent(context: Context): Intent =
    Intent(context, CreateChatActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
