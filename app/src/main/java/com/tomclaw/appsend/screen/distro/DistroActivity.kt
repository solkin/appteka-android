package com.tomclaw.appsend.screen.distro

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.greysonparrelli.permiso.Permiso
import com.greysonparrelli.permiso.Permiso.IOnPermissionResult
import com.greysonparrelli.permiso.Permiso.IOnRationaleProvided
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.distro.di.DistroModule
import com.tomclaw.appsend.screen.permissions.createPermissionsActivityIntent
import com.tomclaw.appsend.screen.upload.createUploadActivityIntent
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.IntentHelper
import com.tomclaw.appsend.util.ThemeHelper
import java.io.File
import javax.inject.Inject

class DistroActivity : AppCompatActivity(), DistroPresenter.DistroRouter {

    @Inject
    lateinit var presenter: DistroPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var preferences: DistroPreferencesProvider

    private val invalidateDetailsResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.invalidateApps()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .distroComponent(DistroModule(this, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)
        Permiso.getInstance().setActivity(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.distro_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = DistroViewImpl(window.decorView, preferences, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-distro-screen")
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
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults)
    }

    override fun openUploadScreen(pkg: UploadPackage, apk: UploadApk) {
        val intent = createUploadActivityIntent(context = this, pkg, apk, info = null)
        startActivity(intent)
        analytics.trackEvent("distro-app-upload")
    }

    override fun searchGooglePlay(packageName: String) {
        try {
            val intent = Intent(ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            invalidateDetailsResultLauncher.launch(intent)
        } catch (ex: ActivityNotFoundException) {
            val intent = Intent(
                ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(intent)
        }
        analytics.trackEvent("distro-open-google-play")
    }

    override fun searchAppteka(packageName: String, title: String) {
        val intent = createDetailsActivityIntent(
            context = this,
            packageName = packageName,
            label = title,
            moderation = false,
            finishOnly = true
        )
        invalidateDetailsResultLauncher.launch(intent)
        analytics.trackEvent("distro-search-appteka")
    }

    override fun installApp(path: String) {
        val intent = IntentHelper.openFileIntent(
            this,
            path,
            "application/vnd.android.package-archive"
        )
        startActivity(intent)
        analytics.trackEvent("distro-install-app")
    }

    override fun openShareApk(path: String) {
        val file = File(path)
        val uri = createFileExternalUri(file)
        val intent = Intent()
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, file.getName())
            .putExtra(Intent.EXTRA_STREAM, uri)
            .setType("application/zip")
        grantUriPermission(uri)
        startActivity(
            Intent.createChooser(
                intent,
                resources.getText(R.string.send_to)
            )
        )
        analytics.trackEvent("distro-share-apk")
    }

    override fun openShareBluetooth(path: String) {
        val file = File(path)
        val uri = createFileExternalUri(file)
        val intent = Intent()
            .setAction(Intent.ACTION_SEND)
            .putExtra(Intent.EXTRA_TEXT, file.name)
            .putExtra(Intent.EXTRA_STREAM, uri)
            .setType("application/zip")
            .setPackage("com.android.bluetooth")
        grantUriPermission(uri)
        startActivity(
            Intent.createChooser(
                intent,
                resources.getText(R.string.send_to)
            )
        )
        analytics.trackEvent("distro-share-bluetooth")
    }

    override fun openPermissionsScreen(permissions: List<String>) {
        val intent = createPermissionsActivityIntent(context = this, permissions)
        startActivity(intent)
        analytics.trackEvent("distro-open-permissions")
    }

    override fun requestStoragePermissions(callback: (Boolean) -> Unit) {
        Permiso.getInstance().requestPermissions(object : IOnPermissionResult {
            override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    callback(true)
                } else {
                    callback(false)
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

    override fun leaveScreen() {
        finish()
    }

    private fun createFileExternalUri(file: File): Uri {
        return if (useFileProvider()) {
            FileProvider.getUriForFile(this, "$packageName.provider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    private fun grantUriPermission(uri: Uri) {
        if (useFileProvider()) {
            val resInfoList: List<ResolveInfo> = packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    private fun useFileProvider(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

}

fun createDistroActivityIntent(
    context: Context,
): Intent = Intent(context, DistroActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
