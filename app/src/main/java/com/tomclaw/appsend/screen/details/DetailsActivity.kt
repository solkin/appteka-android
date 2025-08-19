package com.tomclaw.appsend.screen.details

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.greysonparrelli.permiso.Permiso
import com.greysonparrelli.permiso.Permiso.IOnPermissionResult
import com.greysonparrelli.permiso.Permiso.IOnRationaleProvided
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.download.createDownloadIntent
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.chat.createChatActivityIntent
import com.tomclaw.appsend.screen.details.di.DETAILS_ADAPTER_PRESENTER
import com.tomclaw.appsend.screen.details.di.DetailsModule
import com.tomclaw.appsend.screen.gallery.GalleryItem
import com.tomclaw.appsend.screen.gallery.createGalleryActivityIntent
import com.tomclaw.appsend.screen.home.createHomeActivityIntent
import com.tomclaw.appsend.screen.permissions.createPermissionsActivityIntent
import com.tomclaw.appsend.screen.profile.createProfileActivityIntent
import com.tomclaw.appsend.screen.rate.createRateActivityIntent
import com.tomclaw.appsend.screen.ratings.createRatingsActivityIntent
import com.tomclaw.appsend.screen.upload.createUploadActivityIntent
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.openFileIntent
import com.tomclaw.appsend.util.updateTheme
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import androidx.core.net.toUri
import com.tomclaw.appsend.screen.unlink.createUnlinkActivityIntent
import com.tomclaw.appsend.screen.unpublish.createUnpublishActivityIntent

class DetailsActivity : AppCompatActivity(), DetailsPresenter.DetailsRouter {

    @Inject
    lateinit var presenter: DetailsPresenter

    @Inject
    @Named(DETAILS_ADAPTER_PRESENTER)
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var preferences: DetailsPreferencesProvider

    @Inject
    lateinit var analytics: Analytics

    private val invalidateDetailsResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.invalidateDetails()
            }
        }

    private val backPressedResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.onBackPressed()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        var appId: String? = null
        var packageName: String? = null
        var moderation = false
        var finishOnly = false

        val data = intent.data
        if (data != null && data.host != null) {
            if (data.host == "appteka.store") {
                val path = data.pathSegments
                if (path.size == 2) {
                    when (path[0]) {
                        "app" -> appId = path[1]
                        "package" -> packageName = path[1]
                        else -> throw IllegalArgumentException("Unsupported URL type")
                    }
                }
            } else if (data.host == "appsend.store") {
                appId = data.getQueryParameter("id")
                packageName = data.getQueryParameter("package")
            } else if (data.host == "play.google.com") {
                packageName = data.getQueryParameter("id")
            }
        } else {
            appId = intent.getStringExtra(EXTRA_APP_ID)
            packageName = intent.getStringExtra(EXTRA_PACKAGE)
            moderation = intent.getBooleanExtra(EXTRA_MODERATION, false)
            finishOnly = intent.getBooleanExtra(EXTRA_FINISH_ONLY, false)
        }
        appId
            ?: packageName
            ?: throw IllegalArgumentException("appId or packageName must be provided")

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .detailsComponent(
                DetailsModule(
                    appId,
                    packageName,
                    moderation,
                    finishOnly,
                    this,
                    presenterState
                )
            )
            .inject(activity = this)
        updateTheme()
        Permiso.getInstance().setActivity(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = DetailsViewImpl(window.decorView, preferences, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-details-screen")
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.onBackPressed()
            }
        })
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        overridePendingTransition(0, 0)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        overridePendingTransition(0, 0)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        presenter.attachRouter(this)
    }

    override fun onResume() {
        super.onResume()
        Permiso.getInstance().setActivity(this)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun leaveScreen() {
        finish()
    }

    override fun leaveModeration() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun requestStoragePermissions(callback: () -> Unit) {
        Permiso.getInstance().requestPermissions(object : IOnPermissionResult {
            override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    callback()
                } else {
                    presenter.showSnackbar(getString(R.string.write_permission_install))
                }
            }

            override fun onRationaleRequested(
                callback: IOnRationaleProvided,
                vararg permissions: String
            ) {
                val title: String = getString(R.string.app_name)
                val message: String = getString(R.string.write_permission_install)
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback)
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun openPermissionsScreen(permissions: List<String>) {
        val intent = createPermissionsActivityIntent(context = this, permissions)
        startActivity(intent)
    }

    override fun openRatingsScreen(appId: String) {
        val intent = createRatingsActivityIntent(context = this, appId)
        startActivity(intent)
    }

    override fun openProfile(userId: Int) {
        val intent = createProfileActivityIntent(context = this, userId)
        startActivity(intent)
    }

    override fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            presenter.showSnackbar(resources.getString(R.string.non_launchable_package))
        } else {
            startActivity(intent)
        }
        analytics.trackEvent("details-launch-app")
    }

    override fun installApp(file: File) {
        val intent = openFileIntent(
            filePath = file.absolutePath,
            type = "application/vnd.android.package-archive"
        )
        startActivity(intent)
        analytics.trackEvent("details-install-app")
    }

    override fun removeApp(packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Permiso.getInstance().requestPermissions(object : IOnPermissionResult {
                override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                    if (resultSet.isPermissionGranted(Manifest.permission.REQUEST_DELETE_PACKAGES)) {
                        onRemoveAppPermitted(packageName)
                    } else {
                        presenter.showSnackbar(getString(R.string.request_delete_packages))
                    }
                }

                override fun onRationaleRequested(
                    callback: IOnRationaleProvided,
                    vararg permissions: String
                ) {
                    val title: String = getString(R.string.app_name)
                    val message: String = getString(R.string.request_delete_packages)
                    Permiso.getInstance().showRationaleInDialog(title, message, null, callback)
                }
            }, Manifest.permission.REQUEST_DELETE_PACKAGES)
        } else {
            onRemoveAppPermitted(packageName)
        }
    }

    override fun openRateScreen(
        appId: String,
        userBrief: UserBrief,
        rating: Float,
        review: String?,
        label: String?,
        icon: String?
    ) {
        val intent = createRateActivityIntent(
            context = this,
            appId = appId,
            userBrief = userBrief,
            rating = rating,
            review = review,
            label = label,
            icon = icon,
        )
        invalidateDetailsResultLauncher.launch(intent)
    }

    override fun openEditMetaScreen(
        appId: String,
        label: String?,
        icon: String?,
        packageName: String,
        sha1: String,
        size: Long,
    ) {
        val pkg = UploadPackage(
            uniqueId = appId,
            sha1 = sha1,
            packageName = packageName,
            size = size,
        )
        val intent = createUploadActivityIntent(this, pkg, null, null)
        invalidateDetailsResultLauncher.launch(intent)
    }

    override fun openUnpublishScreen(appId: String, label: String?) {
        val intent = createUnpublishActivityIntent(context = this, appId, label)
        invalidateDetailsResultLauncher.launch(intent)
    }

    override fun openUnlinkScreen(appId: String, label: String?) {
        val intent = createUnlinkActivityIntent(this, appId, label)
        backPressedResultLauncher.launch(intent)
    }

    override fun openAbuseScreen(url: String, label: String?, text: String) {
        val addr = "support@appteka.store"
        val subject = "Abuse Report on $label"

        val uri = Uri.fromParts("mailto", addr, null)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text)
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.send_email)))
        } catch (ex: Throwable) {
            Toast.makeText(this, getString(R.string.no_email_clients), Toast.LENGTH_SHORT).show()
        }
    }

    override fun openDetailsScreen(appId: String, label: String?) {
        val intent = createDetailsActivityIntent(this, appId, label = label.orEmpty())
        startActivity(intent)
        finish()
    }

    override fun openChatScreen(topicId: Int, label: String?) {
        val intent = createChatActivityIntent(this, topicId, label)
        startActivity(intent)
        analytics.trackEvent("details-open-chat")
    }

    override fun openStoreScreen() {
        val intent = createHomeActivityIntent(context = this)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    override fun openGooglePlay(packageName: String) {
        try {
            val intent = Intent(ACTION_VIEW, "market://details?id=$packageName".toUri())
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            val intent = Intent(
                ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$packageName".toUri()
            )
            startActivity(intent)
        }
        analytics.trackEvent("details-open-google-play")
    }

    override fun startDownload(
        label: String,
        version: String,
        icon: String?,
        appId: String,
        url: String
    ) {
        val intent = createDownloadIntent(context = this, label, version, icon, appId, url)
        startService(intent)
        analytics.trackEvent("details-download-app")
    }

    override fun openShare(title: String, text: String) {
        val intent = Intent()
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, text)
            .setType("text/plain")
        startActivity(Intent.createChooser(intent, title))
        analytics.trackEvent("details-share")
    }

    override fun openLoginScreen() {
        val intent = createRequestCodeActivityIntent(context = this)
        startActivity(intent)
    }

    override fun openGallery(items: List<GalleryItem>, current: Int) {
        val intent = createGalleryActivityIntent(context = this, items, current)
        startActivity(intent)
    }

    private fun onRemoveAppPermitted(packageName: String) {
        val packageUri = "package:$packageName".toUri()
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        startActivity(uninstallIntent)
        analytics.trackEvent("details-delete-app")
    }

}

fun createDetailsActivityIntent(
    context: Context,
    appId: String? = null,
    packageName: String? = null,
    label: String,
    moderation: Boolean = false,
    finishOnly: Boolean = false,
): Intent = Intent(context, DetailsActivity::class.java)
    .putExtra(EXTRA_APP_ID, appId)
    .putExtra(EXTRA_PACKAGE, packageName)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_MODERATION, moderation)
    .putExtra(EXTRA_FINISH_ONLY, finishOnly)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_PACKAGE = "package_name"
private const val EXTRA_LABEL = "label"
private const val EXTRA_MODERATION = "moderation"
private const val EXTRA_FINISH_ONLY = "finishOnly"
private const val KEY_PRESENTER_STATE = "presenter_state"
