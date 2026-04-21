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
import com.tomclaw.appsend.di.PICKED_MEDIA_CACHE
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.chat.createChatActivityIntent
import com.tomclaw.appsend.screen.create_chat.di.CreateChatModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.cache.DiskLruCache
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

class CreateChatActivity : AppCompatActivity(), CreateChatPresenter.CreateChatRouter {

    @Inject
    lateinit var presenter: CreateChatPresenter

    @Inject
    lateinit var analytics: Analytics

    @Inject
    @field:Named(PICKED_MEDIA_CACHE)
    lateinit var pickedMediaCache: DiskLruCache

    private val pickAvatarLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Photo Picker URIs carry a session-scoped read grant that does not
            // always survive process death; copy the bytes to app cache so the
            // avatar stays reachable across auth redirects and recreation.
            val cached = uri?.let { copyAvatarToCache(it) } ?: return@registerForActivityResult
            presenter.onAvatarPicked(Uri.fromFile(cached))
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
        loginLauncher.launch(createRequestCodeActivityIntent(this))
    }

    private fun launchAvatarPicker() {
        val request = PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
            .build()
        pickAvatarLauncher.launch(request)
    }

    private fun copyAvatarToCache(source: Uri): File? {
        val temp = File.createTempFile("avatar_pick_", ".jpg", cacheDir)
        return try {
            contentResolver.openInputStream(source)?.use { input ->
                temp.outputStream().use { output -> input.copyTo(output) }
            } ?: return null
            pickedMediaCache.put(AVATAR_CACHE_KEY, temp)
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

}

fun createCreateChatActivityIntent(context: Context): Intent =
    Intent(context, CreateChatActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val AVATAR_CACHE_KEY = "create_chat_avatar"
