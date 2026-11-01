package com.tomclaw.appsend.install

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.download.DownloadNotifications
import com.tomclaw.appsend.util.Analytics
import javax.inject.Inject

/**
 * Says why an install failed and offers the way out. A screen of its own because a
 * failure can surface anywhere, long after the screen that started the install is gone.
 */
class InstallFailureActivity : AppCompatActivity() {

    @Inject
    lateinit var appInstaller: AppInstaller

    @Inject
    lateinit var notifications: DownloadNotifications

    @Inject
    lateinit var analytics: Analytics

    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(activity = this)

        val key = intent.getStringExtra(EXTRA_KEY)
        val label = intent.getStringExtra(EXTRA_LABEL)
        val failure = InstallFailure.entries.firstOrNull {
            it.name == intent.getStringExtra(EXTRA_FAILURE)
        }
        if (key == null || label == null || failure == null) {
            finish()
            return
        }

        // This screen says the same, and more
        notifications.cancelInstall(key)

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.install_failed_title, label))
            .setMessage(failure.message)
            .setOnDismissListener { finish() }
        val action = action(failure, key, label)
        if (action == null) {
            builder.setPositiveButton(R.string.ok, null)
        } else {
            builder.setPositiveButton(action.title) { _, _ -> action.run() }
            builder.setNegativeButton(R.string.cancel, null)
        }
        dialog = builder.show()
    }

    override fun onDestroy() {
        // Dismissing on the way to a recreation must not finish the new instance's work
        dialog?.setOnDismissListener(null)
        dialog?.dismiss()
        super.onDestroy()
    }

    private fun action(failure: InstallFailure, key: String, label: String): Action? {
        return when (failure.action) {
            InstallFailure.Action.NONE -> null

            InstallFailure.Action.REMOVE -> intent.getStringExtra(EXTRA_PACKAGE)?.let { name ->
                Action(R.string.remove) {
                    open(Intent(Intent.ACTION_DELETE, "package:$name".toUri()))
                }
            }

            InstallFailure.Action.FREE_SPACE -> Action(R.string.install_action_free_space) {
                open(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
            }

            InstallFailure.Action.SYSTEM_INSTALLER -> intent.data?.let { uri ->
                Action(R.string.install_method_system) {
                    appInstaller.installWithSystem(InstallRequest(key, label, uri))
                }
            }
        }
    }

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            analytics.trackException(ex, mapOf("reason" to "No screen for install failure action"))
        }
    }

    private class Action(
        @param:StringRes val title: Int,
        val run: () -> Unit,
    )

}

/**
 * [packageToRemove] is what stands in the way, when the failure is about that. Without it,
 * or without [uri], the dialog simply doesn't offer the action that would need it.
 */
fun createInstallFailureIntent(
    context: Context,
    key: String,
    label: String,
    uri: Uri?,
    failure: InstallFailure,
    packageToRemove: String?,
): Intent = Intent(context, InstallFailureActivity::class.java)
    // As data, not an extra: extras don't tell intents apart, and Android would bring
    // back the dialog of one failure instead of showing another
    .setData(uri)
    .putExtra(EXTRA_KEY, key)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_FAILURE, failure.name)
    .putExtra(EXTRA_PACKAGE, packageToRemove)

private const val EXTRA_KEY = "key"
private const val EXTRA_LABEL = "label"
private const val EXTRA_FAILURE = "failure"
private const val EXTRA_PACKAGE = "package"
