package com.tomclaw.appsend.screen.avatar_crop

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.screen.avatar_crop.di.AvatarCropModule
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

class AvatarCropActivity : AppCompatActivity(), AvatarCropPresenter.AvatarCropRouter {

    @Inject
    lateinit var presenter: AvatarCropPresenter

    @Inject
    lateinit var analytics: Analytics

    private lateinit var cacheKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        val sourceUri = intent.getParcelableExtra<Uri>(EXTRA_SOURCE_URI)
            ?: error("AvatarCropActivity launched without source URI")

        // Fresh cache key per launch so each crop yields a unique LRU
        // entry — the resulting file:// URI is therefore unique too,
        // which the avatar preview's image loader needs to bypass its
        // URL-keyed cache. Persisted across recreations so a
        // configuration change mid-crop keeps writing to the same LRU
        // slot the presenter was wired up with.
        cacheKey = savedInstanceState?.getString(KEY_CACHE_KEY)
            ?: "avatar_crop_${System.currentTimeMillis()}"

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .avatarCropComponent(AvatarCropModule(this, sourceUri, cacheKey, presenterState))
            .inject(activity = this)

        // Mirror GalleryActivity: force a dark system-bar appearance so
        // the back/done icons read as light glyphs over the black
        // backdrop and the status bar shows the same translucent
        // overlay as the rest of the dark-themed surface.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.avatar_crop_activity)

        val view = AvatarCropViewImpl(window.decorView)
        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-avatar-crop-screen")
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
        outState.putString(KEY_CACHE_KEY, cacheKey)
    }

    override fun leaveScreen(success: Boolean, croppedUri: Uri?) {
        if (success && croppedUri != null) {
            val data = Intent().apply { putExtra(EXTRA_RESULT_URI, croppedUri) }
            setResult(RESULT_OK, data)
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

}

fun createAvatarCropActivityIntent(context: Context, sourceUri: Uri): Intent =
    Intent(context, AvatarCropActivity::class.java).apply {
        putExtra(EXTRA_SOURCE_URI, sourceUri)
    }

fun extractAvatarCropResultUri(data: Intent?): Uri? =
    data?.getParcelableExtra(EXTRA_RESULT_URI)

private const val KEY_PRESENTER_STATE = "presenter_state"
private const val KEY_CACHE_KEY = "cache_key"
private const val EXTRA_SOURCE_URI = "source_uri"
private const val EXTRA_RESULT_URI = "result_uri"
