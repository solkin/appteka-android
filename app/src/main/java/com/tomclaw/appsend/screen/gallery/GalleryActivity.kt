package com.tomclaw.appsend.screen.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.gallery.di.GalleryModule
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.getParcelableArrayListCompat
import javax.inject.Inject


class GalleryActivity : AppCompatActivity(), GalleryPresenter.GalleryRouter {

    @Inject
    lateinit var presenter: GalleryPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    private val saveFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let { uri ->
                    presenter.onSaveCurrentScreenshot(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val items = intent.getParcelableArrayListCompat(EXTRA_ITEMS, GalleryItem::class.java)
            ?: throw IllegalArgumentException("Extra items must be provided")
        val startIndex = intent.getIntExtra(EXTRA_START_INDEX, 0)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .galleryComponent(GalleryModule(this, items, startIndex, presenterState))
            .inject(activity = this)
        setTheme(R.style.AppThemeSemitransparent)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = GalleryViewImpl(window.decorView, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-gallery-screen")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        presenter.onBackPressed()
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

    override fun openSaveScreenshotDialog(fileName: String, fileType: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = fileType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }
        saveFileLauncher.launch(intent)
    }

    override fun leaveScreen(success: Boolean) {
        if (success) {
            setResult(Activity.RESULT_OK)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

}

fun createGalleryActivityIntent(
    context: Context,
    items: List<GalleryItem>,
    startIndex: Int,
): Intent = Intent(context, GalleryActivity::class.java)
    .putParcelableArrayListExtra(EXTRA_ITEMS, ArrayList(items))
    .putExtra(EXTRA_START_INDEX, startIndex)

private const val EXTRA_ITEMS = "items"
private const val EXTRA_START_INDEX = "start_index"
private const val KEY_PRESENTER_STATE = "presenter_state"
