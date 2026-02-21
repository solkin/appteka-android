package com.tomclaw.appsend.screen.upload

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tomclaw.appsend.util.adapter.ItemBinder
import com.tomclaw.appsend.util.adapter.AdapterPresenter
import com.tomclaw.appsend.util.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.agreement.createAgreementActivityIntent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.gallery.createGalleryActivityIntent
import com.tomclaw.appsend.screen.installed.createInstalledActivityIntent
import com.tomclaw.appsend.screen.upload.di.UPLOAD_ADAPTER_PRESENTER
import com.tomclaw.appsend.screen.upload.di.UploadModule
import com.tomclaw.appsend.screen.upload.dto.UploadScreenshot
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadInfo
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.upload.createUploadIntent
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.getParcelableExtraCompat
import javax.inject.Inject
import javax.inject.Named

class UploadActivity : AppCompatActivity(), UploadPresenter.UploadRouter {

    @Inject
    lateinit var presenter: UploadPresenter

    @Inject
    @Named(UPLOAD_ADAPTER_PRESENTER)
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var preferences: UploadPreferencesProvider

    @Inject
    lateinit var analytics: Analytics

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.onAuthorized()
            }
        }

    private val agreementLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_OK -> presenter.onAgreementAccepted()
                else -> presenter.onAgreementDeclined()
            }
        }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.getUris()?.let { uris ->
                val items: List<UploadScreenshot> = uris.map { convertUri(it) }
                presenter.onImagesSelected(items)
            }
        }

    private val appPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.getUris()?.firstOrNull()?.let { uri ->
                presenter.onFileSelected(uri)
            }
        }

    private var pendingUpload: UploadParams? = null

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            // Start upload regardless of permission result - notifications just won't show
            // Delay is needed to let the system apply the permission before starting foreground service
            Handler(Looper.getMainLooper()).postDelayed({
                pendingUpload?.let { params ->
                    doStartUpload(params.pkg, params.apk, params.info)
                }
                pendingUpload = null
            }, PERMISSION_APPLY_DELAY)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pkg = intent.getParcelableExtraCompat(EXTRA_PACKAGE_INFO, UploadPackage::class.java)
        val apk = intent.getParcelableExtraCompat(EXTRA_APK_INFO, UploadApk::class.java)
        val info = intent.getParcelableExtraCompat(EXTRA_UPLOAD_INFO, UploadInfo::class.java)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        appComponent
            .uploadComponent(UploadModule(this, pkg, apk, info, presenterState))
            .inject(activity = this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = UploadViewImpl(window.decorView, preferences, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-upload-screen")
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

    override fun openApkPicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            action = Intent.ACTION_GET_CONTENT
            type = "application/vnd.android.package-archive"
        }
        appPickerLauncher.launch(intent)
    }

    override fun openInstalledPicker() {
        val intent = createInstalledActivityIntent(context = this, picker = true)
        appPickerLauncher.launch(intent)
    }

    override fun openDetailsScreen(appId: String, label: String?, isFinish: Boolean) {
        val intent = createDetailsActivityIntent(
            context = this,
            appId = appId,
            label = label.orEmpty(),
            finishOnly = true
        )
        if (isFinish) {
            intent.flags = FLAG_ACTIVITY_CLEAR_TOP
            finish()
        }
        startActivity(intent)
    }

    override fun startUpload(pkg: UploadPackage, apk: UploadApk?, info: UploadInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                pendingUpload = UploadParams(pkg, apk, info)
                notificationPermissionLauncher.launch(permission)
                return
            }
        }
        doStartUpload(pkg, apk, info)
    }

    private fun doStartUpload(pkg: UploadPackage, apk: UploadApk?, info: UploadInfo) {
        val intent = createUploadIntent(context = this, pkg, apk, info)
        ContextCompat.startForegroundService(this, intent)
        analytics.trackEvent("upload-start")
    }

    private data class UploadParams(
        val pkg: UploadPackage,
        val apk: UploadApk?,
        val info: UploadInfo
    )

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

    override fun openAgreementScreen() {
        val intent = createAgreementActivityIntent(context = this)
        agreementLauncher.launch(intent)
    }

    override fun leaveScreen() {
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

    private fun convertUri(uri: Uri): UploadScreenshot {
        val input = contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(input, null, options)
        return UploadScreenshot(
            scrId = null,
            original = uri,
            preview = uri,
            width = options.outWidth,
            height = options.outHeight
        )
    }

    private fun ActivityResult.getUris(): List<Uri>? {
        if (resultCode == RESULT_OK) {
            val uris: MutableList<Uri> = ArrayList()
            val clipData = data?.clipData
            if (clipData != null) {
                val count = clipData.itemCount
                for (i in 0 until count) {
                    val item = clipData.getItemAt(i)
                    uris.add(item.uri)
                }
            } else {
                val intentData = data?.data
                if (intentData != null) {
                    uris.add(intentData)
                }
            }
            return uris
        }
        return null
    }

}

fun createUploadActivityIntent(
    context: Context,
    pkg: UploadPackage?,
    apk: UploadApk?,
    info: UploadInfo?,
): Intent = Intent(context, UploadActivity::class.java)
    .putExtra(EXTRA_PACKAGE_INFO, pkg)
    .putExtra(EXTRA_APK_INFO, apk)
    .putExtra(EXTRA_UPLOAD_INFO, info)

private const val EXTRA_PACKAGE_INFO = "pkg_info"
private const val EXTRA_APK_INFO = "apk_info"
private const val EXTRA_UPLOAD_INFO = "upload_info"
private const val KEY_PRESENTER_STATE = "presenter_state"
private const val PERMISSION_APPLY_DELAY = 100L
