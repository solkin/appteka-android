package com.tomclaw.appsend.screen.installed

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
import android.provider.Settings
import androidx.activity.OnBackPressedCallback
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
import com.tomclaw.appsend.screen.installed.di.InstalledModule
import com.tomclaw.appsend.screen.permissions.createPermissionsActivityIntent
import com.tomclaw.appsend.screen.upload.createUploadActivityIntent
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.util.Analytics
import com.tomclaw.appsend.util.updateTheme
import java.io.File
import javax.inject.Inject

class InstalledActivity : AppCompatActivity(), InstalledPresenter.InstalledRouter {

    @Inject
    lateinit var presenter: InstalledPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var preferences: InstalledPreferencesProvider

    private val invalidateDetailsResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.invalidateApps()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .installedComponent(InstalledModule(this, presenterState))
            .inject(activity = this)
        updateTheme()
        Permiso.getInstance().setActivity(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.installed_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = InstalledViewImpl(window.decorView, preferences, adapter)

        presenter.attachView(view)

        if (savedInstanceState == null) {
            analytics.trackEvent("open-installed-screen")
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                presenter.onBackPressed()
            }
        })
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

    override fun openAppScreen(appId: String, title: String) {
        val intent = createDetailsActivityIntent(
            context = this,
            appId = appId,
            label = title,
            moderation = false,
            finishOnly = true
        )
        invalidateDetailsResultLauncher.launch(intent)
        analytics.trackEvent("installed-app-update")
    }

    override fun openUploadScreen(pkg: UploadPackage, apk: UploadApk) {
        val intent = createUploadActivityIntent(context = this, pkg, apk, info = null)
        startActivity(intent)
        analytics.trackEvent("installed-app-upload")
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
        analytics.trackEvent("installed-open-google-play")
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
        analytics.trackEvent("installed-search-appteka")
    }

    override fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            presenter.showSnackbar(resources.getString(R.string.non_launchable_package))
        } else {
            startActivity(intent)
        }
        analytics.trackEvent("installed-launch-app")
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
        analytics.trackEvent("installed-share-apk")
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
        analytics.trackEvent("installed-share-bluetooth")
    }

    override fun openPermissionsScreen(permissions: List<String>) {
        val intent = createPermissionsActivityIntent(context = this, permissions)
        startActivity(intent)
        analytics.trackEvent("installed-open-permissions")
    }

    override fun openSystemDetailsScreen(packageName: String) {
        val intent = Intent()
            .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .addCategory(Intent.CATEGORY_DEFAULT)
            .setData(Uri.parse("package:$packageName"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        invalidateDetailsResultLauncher.launch(intent)
        analytics.trackEvent("installed-system-details")
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

    override fun requestStoragePermissions(callback: () -> Unit) {
        Permiso.getInstance().requestPermissions(object : IOnPermissionResult {
            override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                if (resultSet.isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    callback()
                } else {
                    presenter.showSnackbar(getString(R.string.write_permission_extract))
                }
            }

            override fun onRationaleRequested(
                callback: IOnRationaleProvided,
                vararg permissions: String
            ) {
                val title: String = getString(R.string.app_name)
                val message: String = getString(R.string.write_permission_extract)
                Permiso.getInstance().showRationaleInDialog(title, message, null, callback)
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    override fun leaveScreen() {
        finish()
    }

    private fun onRemoveAppPermitted(packageName: String) {
        val packageUri = Uri.parse("package:$packageName")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        invalidateDetailsResultLauncher.launch(uninstallIntent)
        analytics.trackEvent("installed-delete-app")
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

fun createInstalledActivityIntent(
    context: Context,
): Intent = Intent(context, InstalledActivity::class.java)

private const val KEY_PRESENTER_STATE = "presenter_state"
