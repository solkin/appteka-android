package com.tomclaw.appsend.install

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.tomclaw.appsend.R
import com.tomclaw.appsend.appComponent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

/**
 * Installs from outside the details screen, such as a notification tap. It has to be
 * a visible screen: Android only lets the system's confirmation open from one.
 */
class InstallActivity : AppCompatActivity() {

    @Inject
    lateinit var appInstaller: AppInstaller

    private var dialog: AlertDialog? = null
    private var subscription: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(activity = this)

        val key = intent.getStringExtra(EXTRA_KEY)
        val label = intent.getStringExtra(EXTRA_LABEL)
        val uri = intent.data
        if (key == null || label == null || uri == null) {
            finish()
            return
        }

        if (savedInstanceState == null) {
            appInstaller.install(InstallRequest(key, label, uri))
        }

        val progress = showDialog(key, label)
        subscription = appInstaller.status(key)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { status ->
                when (status) {
                    is InstallStatus.Writing -> {
                        progress.isIndeterminate = false
                        progress.setProgressCompat(status.percent, true)
                    }

                    InstallStatus.Installing -> {
                        // The indicator refuses to turn indeterminate while it is showing
                        progress.hide()
                        progress.isIndeterminate = true
                        progress.show()
                    }

                    InstallStatus.Idle, InstallStatus.Confirming -> finish()
                }
            }
    }

    override fun onDestroy() {
        subscription?.dispose()
        dialog?.dismiss()
        super.onDestroy()
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

private const val EXTRA_KEY = "key"
private const val EXTRA_LABEL = "label"
