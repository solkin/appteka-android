package com.tomclaw.appsend.screen.upload

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.item.CommonItem
import com.tomclaw.appsend.main.local.SelectLocalAppActivity.SELECTED_ITEM
import com.tomclaw.appsend.main.local.SelectLocalAppActivity.createSelectAppActivity
import com.tomclaw.appsend.screen.auth.request_code.createRequestCodeActivityIntent
import com.tomclaw.appsend.screen.details.createDetailsActivityIntent
import com.tomclaw.appsend.screen.upload.di.UploadModule
import com.tomclaw.appsend.upload.UploadApk
import com.tomclaw.appsend.upload.UploadInfo
import com.tomclaw.appsend.upload.UploadPackage
import com.tomclaw.appsend.upload.createUploadIntent
import com.tomclaw.appsend.util.KeyboardHelper
import com.tomclaw.appsend.util.ThemeHelper
import com.tomclaw.appsend.util.getParcelableExtraCompat
import com.tomclaw.appsend.util.md5
import javax.inject.Inject

class UploadActivity : AppCompatActivity(), UploadPresenter.UploadRouter {

    @Inject
    lateinit var presenter: UploadPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    @Inject
    lateinit var preferences: UploadPreferencesProvider

    private val selectAppResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val info: CommonItem = result.data?.getParcelableExtraCompat(
                    SELECTED_ITEM,
                    CommonItem::class.java
                ) ?: return@registerForActivityResult
                val pkg = UploadPackage(
                    uniqueId = info.path.md5(),
                    sha1 = null,
                    packageName = info.packageName,
                )
                val apk = UploadApk(
                    path = info.path,
                    packageInfo = info.packageInfo,
                    version = info.version,
                    size = info.size,
                )
                presenter.onAppSelected(pkg, apk)
            }
        }

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                presenter.onAuthorized()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val pkg = intent.getParcelableExtra<UploadPackage?>(EXTRA_PACKAGE_INFO)
        val apk = intent.getParcelableExtra<UploadApk?>(EXTRA_APK_INFO)
        val info = intent.getParcelableExtra<UploadInfo?>(EXTRA_UPLOAD_INFO)

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .uploadComponent(UploadModule(this, pkg, apk, info, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = UploadViewImpl(window.decorView, preferences, adapter)

        presenter.attachView(view)
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

    override fun openSelectAppScreen() {
        val intent = createSelectAppActivity(this, null)
        selectAppResultLauncher.launch(intent)
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
        val intent = createUploadIntent(context = this, pkg, apk, info)
        startService(intent)
    }

    override fun openLoginScreen() {
        val intent = createRequestCodeActivityIntent(context = this)
        authLauncher.launch(intent)
    }

    override fun leaveScreen() {
        finish()
    }

    override fun hideKeyboard() {
        KeyboardHelper.hideKeyboard(this)
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
