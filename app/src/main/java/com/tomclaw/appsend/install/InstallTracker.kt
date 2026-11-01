package com.tomclaw.appsend.install

import com.jakewharton.rxrelay3.BehaviorRelay
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.ConcurrentHashMap

class InstallTracker {

    // Touched from the main thread, the install executor and the result receiver
    private val relays = ConcurrentHashMap<String, BehaviorRelay<InstallStatus>>()

    fun status(key: String): Observable<InstallStatus> = relay(key).distinctUntilChanged()

    fun update(key: String, status: InstallStatus) {
        relay(key).accept(status)
    }

    private fun relay(key: String): BehaviorRelay<InstallStatus> = relays.getOrPut(key) {
        BehaviorRelay.createDefault(InstallStatus.Idle)
    }

}
