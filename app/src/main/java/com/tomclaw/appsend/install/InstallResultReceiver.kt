package com.tomclaw.appsend.install

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tomclaw.appsend.appComponent
import javax.inject.Inject

/** Declared in the manifest: the system's answer may arrive after the process has died. */
class InstallResultReceiver : BroadcastReceiver() {

    @Inject
    lateinit var sessionInstallMethod: SessionInstallMethod

    override fun onReceive(context: Context, intent: Intent) {
        context.appComponent.inject(receiver = this)
        sessionInstallMethod.onResult(intent)
    }

}
