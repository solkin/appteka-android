package com.tomclaw.appsend.screen.post

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.feed.EXTRA_POST_ID
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.gallery.createGalleryActivityIntent
import com.tomclaw.appsend.screen.post.di.POST_ADAPTER_PRESENTER
import com.tomclaw.appsend.screen.post.di.PostModule
import com.tomclaw.appsend.screen.post.dto.PostImage
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject
import javax.inject.Named

class PostActivity : AppCompatActivity(), PostPresenter.PostRouter {

    @Inject
    lateinit var presenter: PostPresenter

    @Inject
    @Named(POST_ADAPTER_PRESENTER)
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var preferences: PostPreferencesProvider

    @Inject
    lateinit var analytics: Analytics

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.onAuthorized()
            }
        }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uris: MutableList<Uri> = ArrayList()
                val clipData = result.data?.clipData
                if (clipData != null) {
                    val count = clipData.itemCount
                    for (i in 0 until count) {
                        val item = clipData.getItemAt(i)
                        uris.add(item.uri)
                    }
                } else {
                    val intentData = result.data?.data
                    if (intentData != null) {
                        uris.add(intentData)
                    }
                }
                val items: List<PostImage> = uris.map { convertUri(it) }
                presenter.onImagesSelected(items)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .postComponent(PostModule(this, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = PostViewImpl(window.decorView, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-post-screen")
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

    override fun openLoginScreen() {
        val intent = createRequestCodeActivityIntent(context = this)
        authLauncher.launch(intent)
    }

    override fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    override fun openGallery(items: List<GalleryItem>, current: Int) {
        val intent = createGalleryActivityIntent(context = this, items, current)
        startActivity(intent)
    }

    override fun leaveScreen(postId: Int?) {
        if (postId != null) {
            setResult(RESULT_OK, Intent().putExtra(EXTRA_POST_ID, postId))
        } else {
            setResult(RESULT_CANCELED)
        }
        finish()
    }

    override fun hideKeyboard() {
        try {
            currentFocus?.let { view ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (ignored: Throwable) {
        }
    }

    private fun convertUri(uri: Uri): PostImage {
        val input = contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(input, null, options)
        return PostImage(
            scrId = null,
            original = uri,
            preview = uri,
            width = options.outWidth,
            height = options.outHeight
        )
    }

}

fun createPostActivityIntent(
    context: Context,
): Intent = Intent(context, PostActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
