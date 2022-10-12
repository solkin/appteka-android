package com.tomclaw.appsend.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable

interface PackageObserver {

    fun observe(packageName: String): Observable<Int>

    fun pickInstalledVersionCode(packageName: String): Int

}

class PackageObserverImpl(
    context: Context,
    private val packageManager: PackageManager
) : PackageObserver {

    private val packages = HashMap<String, BehaviorRelay<Int>>()

    init {
        val filter = IntentFilter()
        filter.addAction("android.intent.action.PACKAGE_ADDED")
        filter.addAction("android.intent.action.PACKAGE_REMOVED")
        filter.addAction("android.intent.action.PACKAGE_INSTALL")
        filter.addAction("android.intent.action.PACKAGE_CHANGED")
        filter.addAction("android.intent.action.PACKAGE_REPLACED")
        filter.addDataScheme("package")
        filter.addCategory("android.intent.category.DEFAULT")

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                println("[packages] Intent: " + intent.action + "/" + intent.dataString)
                val packageName = intent.dataString?.replace("package:", "")
                if (!packageName.isNullOrEmpty()) {
                    packages[packageName]?.let { relay ->
                        val versionCode = pickInstalledVersionCode(packageName)
                        relay.accept(versionCode)
                    }
                }
            }
        }
        context.registerReceiver(receiver, filter)
        println("[packages] Package observing started")
    }

    override fun observe(packageName: String): Observable<Int> {
        val observable = packages[packageName] ?: let {
            val versionCode = pickInstalledVersionCode(packageName)
            val observable = BehaviorRelay.createDefault(versionCode)
            packages[packageName] = observable
            observable
        }
        return observable
            .doOnSubscribe {
                println("[packages] Package $packageName observer subscribed")
            }
            .doFinally {
                println("[packages] Package $packageName observer disposed")
                if (!observable.hasObservers()) {
                    println("[packages] Package $packageName observer removed")
                    packages.remove(packageName)
                }
            }
    }

    override fun pickInstalledVersionCode(packageName: String): Int {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (ex: Throwable) {
            NOT_INSTALLED
        }
    }

}

const val NOT_INSTALLED = -1
