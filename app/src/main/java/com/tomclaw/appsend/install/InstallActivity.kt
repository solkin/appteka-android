package com.tomclaw.appsend.install

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import com.tomclaw.appsend.util.getParcelableExtraCompat
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

/**
 * Installs from outside the app's own screens, such as a notification tap, and shows the
 * system's question about it. It has to be a visible screen: Android only lets the
 * system's screens open from one.
 */
class InstallActivity : AppCompatActivity() {

    @Inject
    lateinit var appInstaller: AppInstaller

    private var dialog: AlertDialog? = null
    private var progress: LinearProgressIndicator? = null
    private var subscription: Disposable? = null
    private var requested = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(activity = this)

        val key = intent.getStringExtra(EXTRA_KEY)
        val label = intent.getStringExtra(EXTRA_LABEL)
        if (key == null || label == null) {
            finish()
            return
        }
        requested = savedInstanceState?.getBoolean(KEY_REQUESTED) == true
        progress = showDialog(key, label)
    }

    override fun onResume() {
        super.onResume()
        val key = intent.getStringExtra(EXTRA_KEY) ?: return
        val label = intent.getStringExtra(EXTRA_LABEL) ?: return
        if (!requested) {
            requested = true
            request(key, label)
        }
        // Whenever the user is back here, a question still waiting for them is shown
        appInstaller.confirm(key)
        if (subscription == null) {
            subscription = appInstaller.status(key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bindStatus(key, it) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_REQUESTED, requested)
    }

    override fun onDestroy() {
        subscription?.dispose()
        dialog?.dismiss()
        super.onDestroy()
    }

    private fun request(key: String, label: String) {
        val confirmation = intent.getParcelableExtraCompat(EXTRA_CONFIRMATION, Intent::class.java)
        val uri = intent.data
        when {
            confirmation != null -> appInstaller.confirm(key, confirmation)
            uri != null -> appInstaller.install(InstallRequest(key, label, uri))
        }
    }

    private fun bindStatus(key: String, status: InstallStatus) {
        val progress = progress ?: return
        when (status) {
            is InstallStatus.Writing -> {
                progress.isIndeterminate = false
                progress.setProgressCompat(status.percent, true)
            }

            InstallStatus.AwaitingConfirmation -> {
                appInstaller.confirm(key)
                progress.showIndeterminate()
            }

            InstallStatus.Installing -> progress.showIndeterminate()

            InstallStatus.Idle -> finish()
        }
    }

    // The indicator refuses to turn indeterminate while it is showing
    private fun LinearProgressIndicator.showIndeterminate() {
        if (isIndeterminate) return
        hide()
        isIndeterminate = true
        show()
    }

    private fun showDialog(key: String, label: String): LinearProgressIndicator {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_install_progress, null)
        dialog = MaterialAlertDialogBuilder(this)
            .setTitle(label)
            .setView(view)
            .setNegativeButton(R.string.cancel) { _, _ -> cancel(key) }
            .setOnCancelListener { cancel(key) }
            .show()
        return view.findViewById(R.id.install_progress)
    }

    private fun cancel(key: String) {
        appInstaller.cancel(key)
        finish()
    }

}

fun createInstallActivityIntent(
    context: Context,
    request: InstallRequest,
): Intent = Intent(context, InstallActivity::class.java)
    // As data, not an extra: extras don't tell intents apart, and Android would bring
    // back the screen of one install instead of starting another
    .setData(request.uri)
    .putExtra(EXTRA_KEY, request.key)
    .putExtra(EXTRA_LABEL, request.label)

/** Shows the system's [confirmation] kept by a notification, even after our process is gone. */
fun createInstallConfirmationIntent(
    context: Context,
    key: String,
    label: String,
    confirmation: Intent,
): Intent = Intent(context, InstallActivity::class.java)
    // The data tells the screens of different installs apart here as well
    .setData(Uri.fromParts(CONFIRMATION_SCHEME, key, null))
    .putExtra(EXTRA_KEY, key)
    .putExtra(EXTRA_LABEL, label)
    .putExtra(EXTRA_CONFIRMATION, confirmation)

private const val EXTRA_KEY = "key"
private const val EXTRA_LABEL = "label"
private const val EXTRA_CONFIRMATION = "confirmation"
private const val KEY_REQUESTED = "requested"
private const val CONFIRMATION_SCHEME = "confirmation"
