package com.tomclaw.appsend.screen.details

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.avito.konveyor.ItemBinder
import com.avito.konveyor.adapter.AdapterPresenter
import com.avito.konveyor.adapter.SimpleRecyclerAdapter
import com.greysonparrelli.permiso.Permiso
import com.greysonparrelli.permiso.Permiso.IOnPermissionResult
import com.greysonparrelli.permiso.Permiso.IOnRationaleProvided
import com.tomclaw.appsend.Appteka
import com.tomclaw.appsend.R
import com.tomclaw.appsend.main.abuse.AbuseActivity.createAbuseActivityIntent
import com.tomclaw.appsend.main.abuse.AbuseActivity_
import com.tomclaw.appsend.main.meta.MetaActivity.createEditMetaActivityIntent
import com.tomclaw.appsend.main.permissions.PermissionsActivity_
import com.tomclaw.appsend.main.permissions.PermissionsList
import com.tomclaw.appsend.main.profile.ProfileActivity_
import com.tomclaw.appsend.main.ratings.RatingsActivity_
import com.tomclaw.appsend.main.unlink.UnlinkActivity.createUnlinkActivityIntent
import com.tomclaw.appsend.main.unlink.UnlinkActivity_
import com.tomclaw.appsend.main.unpublish.UnpublishActivity.createUnpublishActivityIntent
import com.tomclaw.appsend.main.unpublish.UnpublishActivity_
import com.tomclaw.appsend.screen.details.di.DetailsModule
import com.tomclaw.appsend.screen.rate.createRateActivityIntent
import com.tomclaw.appsend.util.IntentHelper
import com.tomclaw.appsend.util.ThemeHelper
import java.io.File
import javax.inject.Inject

class DetailsActivity : AppCompatActivity(), DetailsPresenter.DetailsRouter {

    @Inject
    lateinit var presenter: DetailsPresenter

    @Inject
    lateinit var adapterPresenter: AdapterPresenter

    @Inject
    lateinit var binder: ItemBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        val appId = intent.getStringExtra(EXTRA_APP_ID)
        val packageName = intent.getStringExtra(EXTRA_PACKAGE)
        appId ?: packageName
        ?: throw IllegalArgumentException("appId or packageName must be provided")

        val presenterState = savedInstanceState?.getBundle(KEY_PRESENTER_STATE)
        Appteka.getComponent()
            .detailsComponent(DetailsModule(appId, packageName, this, presenterState))
            .inject(activity = this)
        ThemeHelper.updateTheme(this)
        Permiso.getInstance().setActivity(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.details_activity)

        val adapter = SimpleRecyclerAdapter(adapterPresenter, binder)
        val view = DetailsViewImpl(window.decorView, adapter)

        presenter.attachView(view)
    }

    override fun onBackPressed() {
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_REVIEW_PUBLISH -> if (resultCode == RESULT_OK) presenter.invalidateDetails()
            REQUEST_EDIT_META -> if (resultCode == RESULT_OK) presenter.invalidateDetails()
            REQUEST_UNPUBLISH -> if (resultCode == RESULT_OK) presenter.invalidateDetails()
            REQUEST_UNLINK -> if (resultCode == RESULT_OK) presenter.onBackPressed()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun leaveScreen() {
        finish()
    }

    override fun openPermissionsScreen(permissions: List<String>) {
        PermissionsActivity_.intent(this)
            .permissions(PermissionsList(ArrayList(permissions)))
            .start()
    }

    override fun openRatingsScreen(appId: String) {
        RatingsActivity_.intent(this)
            .appId(appId)
            .start()
    }

    override fun openProfile(userId: Int) {
        ProfileActivity_.intent(this)
            .userId(userId.toLong())
            .start()
    }

    override fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        startActivity(intent)
    }

    override fun installApp(file: File) {
        val intent = IntentHelper.openFileIntent(
            this,
            file.absolutePath,
            "application/vnd.android.package-archive"
        )
        startActivity(intent)
    }

    override fun removeApp(packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Permiso.getInstance().requestPermissions(object : IOnPermissionResult {
                override fun onPermissionResult(resultSet: Permiso.ResultSet) {
                    if (resultSet.areAllPermissionsGranted()) {
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
        rating: Float,
        review: String?,
        label: String?,
        icon: String?
    ) {
        val intent = createRateActivityIntent(
            context = this,
            appId = appId,
            rating = rating,
            review = review,
            label = label,
            icon = icon,
        )
        startActivityForResult(intent, REQUEST_REVIEW_PUBLISH)
    }

    override fun openEditMetaScreen(
        appId: String,
        label: String?,
        icon: String?,
        packageName: String
    ) {
        val intent = createEditMetaActivityIntent(this, appId, label, icon, packageName)
        startActivityForResult(intent, REQUEST_EDIT_META)
    }

    override fun openUnpublishScreen(appId: String, label: String?) {
        val intent = createUnpublishActivityIntent(this, appId, label)
        startActivityForResult(intent, REQUEST_UNPUBLISH)
    }

    override fun openUnlinkScreen(appId: String, label: String?) {
        val intent = createUnlinkActivityIntent(this, appId, label)
        startActivityForResult(intent, REQUEST_UNLINK)
    }

    override fun openAbuseScreen(appId: String, label: String?) {
        val intent = createAbuseActivityIntent(this, appId, label)
        startActivity(intent)
    }

    private fun onRemoveAppPermitted(packageName: String) {
        val packageUri = Uri.parse("package:$packageName")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
        startActivity(uninstallIntent)
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
    .putExtra(EXTRA_LABEL, label)

private const val EXTRA_APP_ID = "app_id"
private const val EXTRA_PACKAGE = "package_name"
private const val EXTRA_LABEL = "label"
private const val EXTRA_MODERATION = "moderation"
private const val EXTRA_FINISH_ONLY = "finishOnly"
private const val KEY_PRESENTER_STATE = "presenter_state"

private const val REQUEST_REVIEW_PUBLISH = 1
private const val REQUEST_EDIT_META = 2
private const val REQUEST_UNLINK = 3
private const val REQUEST_UNPUBLISH = 4
